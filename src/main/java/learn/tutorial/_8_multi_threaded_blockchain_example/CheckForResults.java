package learn.tutorial._8_multi_threaded_blockchain_example;

import learn.tutorial._6_blockchain_core.HashResult;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CheckForResults implements Runnable {
	
	private HashResult hashResult;

	@Override
	public void run() {
		while (!hashResult.isComplete()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
