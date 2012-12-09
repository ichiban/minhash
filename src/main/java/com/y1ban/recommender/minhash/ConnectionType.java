package com.y1ban.recommender.minhash;

import org.jeromq.ZMQ.Socket;

public enum ConnectionType {
	BIND {
		@Override
		public void bindOrConnect(Socket socket, String addr) {
			socket.bind(addr);
		}
	}, CONNECT {
		@Override
		public void bindOrConnect(Socket socket, String addr) {
			socket.connect(addr);			
		}
	};
	
	public abstract void bindOrConnect(Socket socket, String addr);
}
