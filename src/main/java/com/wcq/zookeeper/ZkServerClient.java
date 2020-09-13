package com.wcq.zookeeper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

public class ZkServerClient {
	public static List<String> listServer = new ArrayList<String>();
	public static String parent = "/test";

	public static void main(String[] args) {
		initServer();
		ZkServerClient client = new ZkServerClient();
		BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
		while (true) {
			String name;
			try {
				name = console.readLine();
				if ("exit".equals(name)) {
					System.exit(0);
				}
				client.send(name);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	// 注册所有server
	public static void initServer() {
		// listServer.add("127.0.0.1:18080");

		final ZkClient zkClient = new ZkClient("127.0.0.1:2181", 6000, 1000);
		List<String> children = zkClient.getChildren(parent);
		getChilds(zkClient, children);
		// 监听事件
		zkClient.subscribeChildChanges(parent, new IZkChildListener() {

			public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
				getChilds(zkClient, currentChilds);
			}
		});
	}

	private static void getChilds(ZkClient zkClient, List<String> currentChilds) {
		listServer.clear();
		for (String p : currentChilds) {
			String pathValue = (String) zkClient.readData(parent + "/" + p);
			listServer.add(pathValue);
		}
		serverCount = listServer.size();
		System.out.println("从zk读取到信息:" + listServer.toString());

	}

	// 请求次数
	private static int reqestCount = 1;
	// 服务
	// 数量
	private static int serverCount = 0;

	// 获取当前server信息
	public static String getServer() {
		// 实现负载均衡
		String serverName = listServer.get(reqestCount % serverCount);
		++reqestCount;
		return serverName;
	}

	public void send(String name) {

		String server = ZkServerClient.getServer();
		String[] cfg = server.split(":");

		Socket socket = null;
		BufferedReader in = null;
		PrintWriter out = null;
		try {
			socket = new Socket(cfg[0], Integer.parseInt(cfg[1]));
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);

			out.println(name);
			while (true) {
				String resp = in.readLine();
				if (resp == null)
					break;
				else if (resp.length() > 0) {
					System.out.println("Receive : " + resp);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (out != null) {
				out.close();
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}