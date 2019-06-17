package info.elexis.server.core.internal.service;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import ch.elexis.core.model.IContact;
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
	
	@SuppressWarnings("unchecked")
	@Override
	public <T> Optional<T> getTyped(Class<T> clazz){
		return Optional.ofNullable((T) context.get(clazz.getName()));
	}
	
	@Override
	public void setTyped(Object object){
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
