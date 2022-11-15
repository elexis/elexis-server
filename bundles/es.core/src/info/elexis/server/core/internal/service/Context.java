package info.elexis.server.core.internal.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.LoggerFactory;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContext;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.utils.OsgiServiceUtil;
import info.elexis.server.core.SystemPropertyConstants;

public class Context implements IContext {

	private Context parent;

	private ConcurrentHashMap<String, Object> context;

	public Context() {
		context = new ConcurrentHashMap<>();
		this.parent = null;
	}

	public Context(Context parent) {
		context = new ConcurrentHashMap<>();
		this.parent = parent;
	}

	public void setParent(Object object) {
		this.parent = null;
	}

	public Context getParent() {
		return parent;
	}

	@Override
	public String getStationIdentifier() {
		return SystemPropertyConstants.getStationId();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getTyped(Class<T> clazz) {
		T object = (T) context.get(clazz.getName());
		if (clazz.equals(IMandator.class) && object == null) {
			object = (T) lazyLoadDefaultMandator();
		}
		return Optional.ofNullable(object);
	}

	/**
	 * Not every API request requires the current user {@link IMandator}. So if a
	 * {@link IMandator} is requested, and not set, we determine the default
	 * {@link IMandator} for the user and set it to the context
	 * 
	 * @param <T>
	 * @return the default {@link IMandator} or <code>null</code> if none found
	 */
	private IMandator lazyLoadDefaultMandator() {
		@SuppressWarnings("unchecked")
		Optional<IContact> userContact = (Optional<IContact>) getNamed(ACTIVE_USERCONTACT);
		if (userContact.isPresent()) {
			Optional<IUserService> userService = OsgiServiceUtil.getService(IUserService.class);
			if (userService.isPresent()) {
				Optional<IMandator> defaultMandator = userService.get()
						.getDefaultExecutiveDoctorWorkingFor(userContact.get());
				if (defaultMandator.isPresent()) {
					setTyped(defaultMandator.get());
					return defaultMandator.get();
				}
			} else {
				LoggerFactory.getLogger(getClass()).error("Could not getService IUserService");
			}
		}
		return null;
	}

	@Override
	public void setTyped(Object object) {
		if (object != null) {
			if (object instanceof IUser) {
				// also set active user contact
				IContact userContact = ((IUser) object).getAssignedContact();
				setNamed(ACTIVE_USERCONTACT, userContact);
			}
			Optional<Class<?>> modelInterface = getModelInterface(object);
			if (object.equals(context.get(modelInterface.get().getName()))) {
				// object is already in the context do nothing otherwise loop happens
				return;
			}
			if (modelInterface.isPresent()) {
				context.put(modelInterface.get().getName(), object);
			} else {
				context.put(object.getClass().getName(), object);
			}
		}
	}

	private Optional<Class<?>> getModelInterface(Object object) {
		Class<?>[] interfaces = object.getClass().getInterfaces();
		for (Class<?> interfaze : interfaces) {
			if (interfaze.getName().startsWith("ch.elexis.core.model")
					&& !interfaze.getName().contains("Identifiable")) {
				return Optional.of(interfaze);
			}
		}
		return Optional.empty();
	}

	@Override
	public void removeTyped(Class<?> clazz) {
		context.remove(clazz.getName());
	}

	@Override
	public Optional<?> getNamed(String name) {
		return Optional.ofNullable(context.get(name));

	}

	@Override
	public void setNamed(String name, Object object) {
		if (object == null) {
			context.remove(name);
		} else {
			context.put(name, object);
		}
	}

}
