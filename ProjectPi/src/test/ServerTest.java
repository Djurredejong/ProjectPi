package test;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import server.Server;

class ServerTest {

	private Server server;
	private int port = 9999;

	@BeforeEach
	void setUp() throws Exception {
		server = new Server(port);
	}

	@Test
	void test() throws IOException {

	}
}
