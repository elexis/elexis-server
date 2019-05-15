package info.elexis.server.core.internal.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IContext;
import info.elexis.server.core.SystemPropertyConstants;

public class Context implements IContext {
	
	private ConcurrentHashMap<String, Object> context;
	
	public Context(){
		context = new ConcurrentHashMap<>();
	}
	
	@Override
	public String getStationIdentifier(){
		return SystemPropertyConstants.getStationId();
	}
	
	@Override
	public Optional<IUser> getActiveUser(){
		return Optional.ofNullable((IUser) context.get(ACTIVE_USER));
	}
	
	@Override
	public void setActiveUser(IUser user){
		if (user == null) {
			context.remove(ACTIVE_USER);
		} else {
			setNamed(ACTIVE_USER, user);
		}
	}
	
	@Override
	public Optional<IContact> getActiveUserContact(){
		return Optional.ofNullable((IContact) context.get(ACTIVE_USERCONTACT));
	}
	
	@Override
	public void setActiveUserContact(IContact userContact){
		if (userContact == null) {
			context.remove(ACTIVE_USERCONTACT);
		} else {
			setNamed(ACTIVE_USERCONTACT, userContact);
		}
	}
	
	@Override
	public Optional<IPatient> getActivePatient(){
		return Optional.ofNullable((IPatient) context.get(ACTIVE_PATIENT));
	}
	
	@Override
	public void setActivePatient(IPatient patient){
		if (patient == null) {
			context.remove(ACTIVE_PATIENT);
		} else {
			setNamed(ACTIVE_PATIENT, patient);
		}
	}
	
	@Override
	public Optional<IMandator> getActiveMandator(){
		return Optional.ofNullable((IMandator) context.get(ACTIVE_MANDATOR));
		
	}
	
	@Override
	public void setActiveMandator(IMandator mandator){
		if (mandator == null) {
			context.remove(ACTIVE_MANDATOR);
		} else {
			setNamed(ACTIVE_MANDATOR, mandator);
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getTyped(Class<T> clazz){
		return Optional.ofNullable((T) context.get(clazz.getName()));
	}
	
	@Override
	public void setTyped(Object object){
		if (object != null) {
			Optional<Class<?>> modelInterface = getModelInterface(object);
			if (modelInterface.isPresent()) {
				context.put(modelInterface.get().getName(), object);
			} else {
				context.put(object.getClass().getName(), object);
			}
		}
	}
	
	private Optional<Class<?>> getModelInterface(Object object){
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
	public void removeTyped(Class<?> clazz){
		context.remove(clazz.getName());
	}
	
	@Override
	public Optional<?> getNamed(String name){
		return Optional.ofNullable(context.get(name));
		
	}
	
	@Override
	public void setNamed(String name, Object object){
		if (object == null) {
			context.remove(name);
		} else {
			context.put(name, object);
		}
	}
	
}
