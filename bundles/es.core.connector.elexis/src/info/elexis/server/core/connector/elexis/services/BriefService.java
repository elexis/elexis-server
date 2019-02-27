package info.elexis.server.core.connector.elexis.services;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.LoggerFactory;

import com.google.common.io.Files;

import ch.elexis.core.constants.Preferences;
import ch.rgw.tools.MimeTool;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Brief;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.DocHandle;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Heap;
import info.elexis.server.core.connector.elexis.jpa.model.annotated.Kontakt;

public class BriefService extends PersistenceService {

	public static class Builder extends AbstractBuilder<Brief> {
		/**
		 * Requires a {@link Heap} object with corresponding id, use
		 * {@link #buildAndSave()} to ensure proper creation.
		 * 
		 * @param patient
		 */
		public Builder(Kontakt patient) {
			object = new Brief();
			object.setPatient(patient);
			object.setCreationDate(LocalDateTime.now());
		}
	}

	/**
	 * convenience method, also loading the byte[] content
	 * 
	 * @param id
	 * @return
	 */
	public static Optional<Brief> load(String id) {
		Optional<Brief> brief = PersistenceService.load(Brief.class, id).map(v -> (Brief) v);
		if (brief.isPresent()) {
			brief.get().setContent(briefGetContentConsideringNetworkPathStoreIfRequired(brief.get()));
		}
		return brief;
	}

	/**
	 * Overriding PersistentsService#save stores content
	 * 
	 * @param brief
	 */
	public static Brief save(Brief brief) {
		Brief saved = (Brief) PersistenceService.save(brief);
		saved.setContent(brief.getContent());
		briefStoreContentConsideringNetworkPathStoreIfRequired(saved, saved.getContent());
		return saved;
	}
	
	/**
	 * 
	 * @param brief
	 */
	public static void remove(Brief brief) {
		LoggerFactory.getLogger(BriefService.class).error("Removing brief [{}]", brief.getId());
		PersistenceService.remove(brief);
		getExternFile(brief).ifPresent(File::delete);
		HeapService.load(brief.getId()).ifPresent(PersistenceService::remove);
	}

	/**
	 * Retrieve a brief stored file, considering possible file system storage.
	 * 
	 * @param docHandle
	 * @return
	 */
	private static byte[] briefGetContentConsideringNetworkPathStoreIfRequired(Brief brief) {
		if (isExternFile() && brief.getPatient() != null) {
			Optional<File> file = getExternFile(brief);
			if (file.isPresent()) {
				try {
					return Files.toByteArray(file.get());
				} catch (IOException ioe) {
					LoggerFactory.getLogger(BriefService.class)
							.error("Could not access file for Brief [" + brief.getId() + "]", ioe);
				}
			}
			LoggerFactory.getLogger(BriefService.class)
					.warn("File not present for Brief [" + brief.getId() + "], trying heap content.");
		}

		Optional<Heap> content = HeapService.load(brief.getId());
		if (content.isPresent() && content.get().getInhalt() != null && content.get().getInhalt().length > 0) {
			return content.get().getInhalt();
		}
		LoggerFactory.getLogger(BriefService.class)
				.warn("Brief [{}] content not found or seems to be empty, returning empty byte array.", brief.getId());
		return new byte[] {};
	}

	/**
	 * Store the data for the given {@link DocHandle}. If configured and functional,
	 * the data will be stored at the respective network path location. If not, and
	 * in case of error, data is stored in database.
	 * 
	 * @param docHandle
	 * @param data
	 */
	private static void briefStoreContentConsideringNetworkPathStoreIfRequired(Brief brief, byte[] data) {
		if (isExternFile() && brief.getPatient() != null) {
			// store in file
			Optional<File> externFile = getExternFile(brief);
			if (!externFile.isPresent()) {
				externFile = createExternFile(brief);
			}

			if (externFile.isPresent()) {
				try {
					Files.write(data, externFile.get());
					return;
				} catch (IOException e) {
					LoggerFactory.getLogger(BriefService.class).error("Error writing file", e);
				}
			}
			LoggerFactory.getLogger(BriefService.class)
					.error("Brief [{}] File persist error. Reverting to heap storage.", brief.getId());
		}

		// store in heap
		Optional<Heap> heap = HeapService.load(brief.getId());
		if (!heap.isPresent()) {
			heap = Optional.of(new HeapService.Builder(brief.getId()).build());

		}
		heap.get().setInhalt(data);
		heap.get().setDatum(LocalDate.now());
		HeapService.save(heap.get());
	}

	/**
	 * Create a new extern file for the {@link Brief}.
	 * 
	 * @param brief
	 * @return
	 * @see https://github.com/elexis/elexis-3-core/blob/master/bundles/ch.elexis.core.data/src/ch/elexis/core/data/util/BriefExternUtil.java
	 */
	private static Optional<File> createExternFile(Brief brief) {
		String path = ConfigService.INSTANCE.get(Preferences.P_TEXT_EXTERN_FILE_PATH, null);
		if (isValidExternPath(path, true)) {
			File dir = new File(path);
			Kontakt patient = brief.getPatient();
			if (patient != null) {
				File patPath = new File(dir, patient.getCode());
				if (!patPath.exists()) {
					patPath.mkdirs();
				}
				File ret = new File(patPath, brief.getId() + "." + evaluateExtension(brief.getMimetype()));
				if (!ret.exists()) {
					try {
						ret.createNewFile();
					} catch (IOException e) {
						LoggerFactory.getLogger(BriefService.class).error("Error creating file", e);
						return Optional.empty();
					}
				}
				return Optional.of(ret);
			} else {
				LoggerFactory.getLogger(BriefService.class).warn("No patient for [" + brief.getId() + "]");
			}
		}
		return Optional.empty();
	}

	/**
	 * Get an existing extern {@link File} for the {@link Brief}.
	 * 
	 * @param brief
	 * @return Brief or empty if no such file is found
	 * @see https://github.com/elexis/elexis-3-core/blob/master/bundles/ch.elexis.core.data/src/ch/elexis/core/data/util/BriefExternUtil.java
	 */
	private static Optional<File> getExternFile(Brief brief) {
		String path = ConfigService.INSTANCE.get(Preferences.P_TEXT_EXTERN_FILE_PATH, null);
		if (isValidExternPath(path, true)) {
			File dir = new File(path);
			StringBuilder sb = new StringBuilder();
			Kontakt patient = brief.getPatient();
			if (patient != null) {
				sb.append(patient.getCode()).append(File.separator).append(brief.getId())
						.append("." + evaluateExtension(brief.getMimetype()));
				File ret = new File(dir, sb.toString());
				if (ret.exists() && ret.isFile()) {
					return Optional.of(ret);
				} else {
					LoggerFactory.getLogger(BriefService.class).warn(
							"File [" + ret.getAbsolutePath() + "] not valid e=" + ret.exists() + " f=" + ret.isFile());
				}
			} else {
				LoggerFactory.getLogger(BriefService.class).warn("No patient for [" + brief.getId() + "]");
			}
		}
		return Optional.empty();
	}

	/**
	 * 
	 * @param input
	 * @return
	 * @see https://github.com/elexis/elexis-3-core/blob/master/bundles/ch.elexis.core.data/src/ch/elexis/core/data/util/BriefExternUtil.java
	 */
	private static String evaluateExtension(String input) {
		String ext = MimeTool.getExtension(input);
		if (StringUtils.isEmpty(ext)) {
			ext = FilenameUtils.getExtension(input);
			if (StringUtils.isEmpty(ext)) {
				ext = input;
			}
		}
		return ext;
	}

	/**
	 * Test if configuration of {@link Brief} as extern file is set and valid.
	 * 
	 * @return
	 * @see https://github.com/elexis/elexis-3-core/blob/master/bundles/ch.elexis.core.data/src/ch/elexis/core/data/util/BriefExternUtil.java
	 */
	private static boolean isExternFile() {
		if (ConfigService.INSTANCE.get(Preferences.P_TEXT_EXTERN_FILE, false)) {
			String path = ConfigService.INSTANCE.get(Preferences.P_TEXT_EXTERN_FILE_PATH, null);
			boolean ret = isValidExternPath(path,
					true);
			if (!ret) {
				LoggerFactory.getLogger(BriefService.class)
						.error("Briefe extern speichern aktiviert, aber Pfad [{}] nicht erreichbar.", path);
			}
			return ret;
		}
		return false;
	}

	/**
	 * Test if the configured Path is available.
	 * 
	 * @param string
	 * 
	 * @return
	 * @see https://github.com/elexis/elexis-3-core/blob/master/bundles/ch.elexis.core.data/src/ch/elexis/core/data/util/BriefExternUtil.java
	 */
	private static boolean isValidExternPath(String path, boolean log) {
		if (path != null) {
			File dir = new File(path);
			if (dir.exists() && dir.isDirectory() && dir.canWrite()) {
				return true;
			} else {
				if (log) {
					LoggerFactory.getLogger(BriefService.class).warn("Configured path [" + path + "] not valid e="
							+ dir.exists() + " d=" + dir.isDirectory() + " w=" + dir.canWrite());
				}
			}
		} else if (log) {
			LoggerFactory.getLogger(BriefService.class).warn("No path configured");
		}
		return false;
	}

}
