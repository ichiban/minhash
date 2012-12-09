package com.y1ban.recommender.minhash;

import java.util.Iterator;
import java.util.List;

import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Socket;

import com.y1ban.recommender.minhash.repository.InstanceRepository;

public enum Request {
	ADD {
		@Override
		public void response(final Socket socket,
				final InstanceRepository repo, final List<Integer> seeds) {
			if (!socket.hasReceiveMore()) {
				// request must have at least ID field
				throw new IllegalStateException();
			}

			final long id = Long.parseLong(socket.recvStr());
			final Instance instance = repo.findById(id);
			int count = 0;

			while (socket.hasReceiveMore()) {
				// rest of the request message is features
				final int feature = Integer.parseInt(socket.recvStr());
				instance.addFeature(feature);
				count++;
			}

			repo.put(instance);

			// response is a number of added features
			socket.send(Integer.toString(count));
		}
	},
	DEL {
		@Override
		public void response(Socket socket, final InstanceRepository repo,
				final List<Integer> seeds) {
			if (!socket.hasReceiveMore()) {
				// request must have at least ID field
				throw new IllegalStateException();
			}

			final long id = Long.valueOf(socket.recvStr());
			final Instance instance = repo.findById(id);
			int count = 0;

			while (socket.hasReceiveMore()) {
				// rest of the request message is features
				final int feature = Integer.valueOf(socket.recvStr());
				instance.removeFeature(feature);
				count++;
			}

			repo.put(instance);

			// response is a number of deleted features
			socket.send(Integer.toString(count));
		}
	},
	GET {
		@Override
		public void response(Socket socket, final InstanceRepository repo,
				final List<Integer> seeds) {
			if (!socket.hasReceiveMore()) {
				// request must have at least ID field
				throw new IllegalStateException();
			}

			final long id = Long.parseLong(socket.recvStr());
			final Instance instance = repo.findById(id);
			final Iterator<Integer> iter = instance.features.iterator();

			// response is features
			while (iter.hasNext()) {
				final String feature = Integer.toString(iter.next());
				if (iter.hasNext()) {
					socket.send(feature, ZMQ.SNDMORE);
				} else {
					socket.send(feature);
				}
			}
		}
	},
	FIND {
		@Override
		public void response(Socket socket, final InstanceRepository repo,
				final List<Integer> seeds) {
			if (!socket.hasReceiveMore()) {
				// request must have at least ID field
				throw new IllegalStateException();
			}

			final long id = Long.parseLong(socket.recvStr());
			final Instance instance = repo.findById(id);

			if (!socket.hasReceiveMore()) {
				// request must have at least MIN_RESEMBLANCE field
				throw new IllegalStateException();
			}

			final double minResemblance = Double.parseDouble(socket.recvStr());

			final List<SimilarInstance> similarInstances = repo.findByHash(
					instance.minhashes(seeds), minResemblance);
			final Iterator<SimilarInstance> iter = similarInstances.iterator();

			// response is a list of pairs of instance id and resemblance
			while (iter.hasNext()) {
				final SimilarInstance similarInstance = iter.next();
				socket.send(Long.toString(similarInstance.instance.id),
						ZMQ.SNDMORE);
				if (iter.hasNext()) {
					socket.send(Double.toString(similarInstance.resemblance),
							ZMQ.SNDMORE);
				} else {
					socket.send(Double.toString(similarInstance.resemblance));
				}
			}
		}
	};

	public abstract void response(final Socket socket,
			final InstanceRepository repo, final List<Integer> seeds);
}
