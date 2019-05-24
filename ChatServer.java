import java.net.*;
import java.io.*;
import java.util.*;

public class ChatServer {

	public static void main(String[] args) {
		try{
			ServerSocket server = new ServerSocket(10001);
			System.out.println("Waiting connection...");
			HashMap hm = new HashMap();
			while(true){
				Socket sock = server.accept();
				ChatThread chatthread = new ChatThread(sock, hm);
				chatthread.start();
			} // while
		}catch(Exception e){
			System.out.println(e);
		}
	} // main
}

class ChatThread extends Thread{
	private Socket sock;
	private String id;
	private BufferedReader br;
	private HashMap hm;
	private boolean initFlag = false;
	private String a,b,c,d,e;
	public ChatThread(Socket sock, HashMap hm){
		this.sock = sock;
		this.hm = hm;
		a = "stupid"; b = "dumb"; c = "I'm inevitable"; d = "I'm Iron man"; e = "Voldemort";
		try{
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()));
			br = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			id = br.readLine();
			broadcast(id + " entered.");
			System.out.println("[Server] User (" + id + ") entered.");
			synchronized(hm){
				hm.put(this.id, pw);
			} // hm에 넣을때 줄을 세우는 역할을 synchronized가 함. -> 여러명이 한꺼번에 들어올려고 할때를 막음.
			initFlag = true; // 원래 의도가 있었는데... 만들다가 말았음.
		}catch(Exception ex){
			System.out.println(ex);
		}
	} // construcor
	public void run(){
		try{
			String line = null;
			while((line = br.readLine()) != null){
				if(line.equals("/quit"))
					break;
				if (line.contains(a) || line.contains(b) || line.contains(c) || line.contains(d) || line.contains(e)) {
					warning(); // 우선순위를 제일 먼저 둠.
				} else if(line.indexOf("/to ") == 0){
						sendmsg(line);
					}else if (line.equals("/userlist")) {
						send_userlist();
					} else {
						broadcast(id + " : " + line);
					}
				}
		}catch(Exception ex){
			System.out.println(ex);
		}finally{
			synchronized(hm){
				hm.remove(id);
			}
			broadcast(id + " exited.");
			try{
				if(sock != null)
					sock.close();
			}catch(Exception ex){}
		}
	} // run
	
	public void warning() {
		PrintWriter pw = (PrintWriter)hm.get(id);
		pw.println("WARNING!: Usage of Inappopriate word!");
		pw.flush();
		pw.println("This will be reported to the admin!");
		pw.flush();
		System.out.println("REPORT: "+id+" USAGE OF INAPPOPRIATE WORD!"); // report to admin
	} // warning to the user
	
	public void sendmsg(String msg){
		int start = msg.indexOf(" ") +1;
		int end = msg.indexOf(" ", start);
		if(end != -1){
			String to = msg.substring(start, end);
			String msg2 = msg.substring(end+1);
			Object obj = hm.get(to); // synchronized 해야하는데 까먹은듯.
			if(obj != null){
				PrintWriter pw = (PrintWriter)obj;
				pw.println(id + " whisphered. : " + msg2);
				pw.flush();
			} // if
		}
	} // sendmsg
	
	public void send_userlist() {
		synchronized(hm){
			Set key = hm.keySet(); // key들 저장.
			int count = 0;
			Iterator iter = key.iterator(); // Iterator 생성
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)hm.get(id);
				pw.println(); pw.flush();
				while(iter.hasNext()) {
					pw.println(iter.next());
					pw.flush();
					count++; // counting user total
				}
				pw.println(); pw.flush();
				pw.println(count+ " users in total!");
				pw.flush();
			}
		}
	} // userlist
	
	public void broadcast(String msg){
		boolean pass = false;
		synchronized(hm){
			Collection collection = hm.values();
			Iterator iter = collection.iterator();
			while(iter.hasNext()){
				PrintWriter pw = (PrintWriter)iter.next();
				if (pw != hm.get(id)) {
					pw.println(msg);
					pw.flush();
				} // 자신 빼고 나머지들에게만 전달!
			}
		}
	} // broadcast
}