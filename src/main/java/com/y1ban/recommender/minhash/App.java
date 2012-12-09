package com.y1ban.recommender.minhash;

import java.util.List;

import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Context;
import org.jeromq.ZMQ.Socket;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;
import com.y1ban.recommender.minhash.repository.InstanceRepository;

public class App {
	@Inject
	InstanceRepository repo;
	@Inject
	@Named("seeds")
	List<Integer> seeds;
	@Inject
	ConnectionType connectionType;
	@Inject
	@Named("address")
	String address;

	public static void main(String[] args) {
		final Injector injector = Guice.createInjector(new DefaultModule());
		final App app = new App();
		injector.injectMembers(app);
		app.run();
	}

	private void run() {
		final Context context = ZMQ.context();
		final Socket socket = context.socket(ZMQ.REP);
		connectionType.bindOrConnect(socket, address);

		try {
			while (true) {
				final Request request = Request.valueOf(socket.recvStr());
				request.response(socket, repo, seeds);
			}
		} finally {
			socket.close();
			context.term();
		}
	}
}
