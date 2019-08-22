package com.uitgis.prototype.globe.util;
import java.util.HashSet;
import java.util.Optional;


public class SessionManager {


	private static SessionManager manager = new SessionManager();
	
	private HashSet<Session> sessions = new HashSet<Session>();
	
	private SessionManager() {
	}
	
	public static SessionManager getInstance() {
		return SessionManager.manager;
	}
	
	public Session getSession(String id) {
		Session session = null;
		Optional<Session> optSession = this.sessions.stream().filter(s -> s.getId().equals(id)).findFirst();
		
		if (optSession.isPresent()) {
			session = optSession.get();
		}
		
		return session;
	}
	
	public void addSession(Session session) {
		this.sessions.add(session);
	}

}