import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class GameServer extends JFrame {

	private JPanel contentPane;
	JTextArea textArea;
	private JTextField txtPortNumber;

	private DatagramSocket udp_socket;

	private ServerSocket socket; // 서버소켓
	private Socket client_socket; // accept() 에서 생성된 client 소켓
	private Vector UserVec = new Vector(); // 연결된 사용자를 저장할 벡터
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
	private int time = 63;

	private int[] deoArr = new int[15];
	private int s = 0;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GameServer frame = new GameServer();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GameServer() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 377, 569);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 22, 339, 395);
		contentPane.add(scrollPane);

		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);

		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(42, 443, 73, 15);
		contentPane.add(lblNewLabel);

		txtPortNumber = new JTextField();
		txtPortNumber.setBounds(156, 434, 174, 34);
		txtPortNumber.setText("30000");
		contentPane.add(txtPortNumber);
		txtPortNumber.setColumns(10);

		JButton btnServerStart = new JButton("Server Start");
		btnServerStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					socket = new ServerSocket(Integer.parseInt(txtPortNumber.getText()));
					udp_socket = new DatagramSocket(Integer.parseInt(txtPortNumber.getText()));
				} catch (NumberFormatException | IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				AppendText("Game Server Running..");
				btnServerStart.setText("Chat Server Running..");
				btnServerStart.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
				txtPortNumber.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(75, 478, 217, 41);
		contentPane.add(btnServerStart);

	}

	// 새로운 참가자 accept() 하고 user thread를 새로 생성한다.
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
					AppendText("새로운 참가자 from " + client_socket);
					// User 당 하나씩 Thread 생성
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user); // 새로운 참가자 배열에 추가
					new_user.start(); // 만든 객체의 스레드 실행
					AppendText("현재 참가자 수 " + UserVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
					// System.exit(0);
				}
			}
		}
	}

	public synchronized void AppendText(String str) {
		// textArea.append("사용자로부터 들어온 메세지 : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public synchronized void AppendObject(ChatMsg msg) {
		// textArea.append("사용자로부터 들어온 object : " + str+"\n");
		textArea.append("[" + msg.getId() + "]" + " " + msg.getCode() + "\n");
		textArea.append("data = " + msg.getData() + "\n");

		textArea.setCaretPosition(textArea.getText().length());
	}

	// User 당 생성되는 Thread
	// Read One 에서 대기 -> Write All
	class UserService extends Thread {

		byte[] bb = new byte[128];
		DatagramPacket udp_packet = new DatagramPacket(bb, bb.length);
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;

		private ObjectInputStream ois;
		private ObjectOutputStream oos;

		private Socket client_socket;
		private Vector user_vc;
		public String UserName = "";
		public String UserStatus;
		public int[] GameWith;
		public int with = -1;
		public int myNum;
		private int score = 0;
		private DeojiThread[] dT = new DeojiThread[4];

		public UserService(Socket client_socket) {
			// TODO Auto-generated constructor stub
			// 매개변수로 넘어온 자료 저장
			this.client_socket = client_socket;
			this.user_vc = UserVec;
			try {
				oos = new ObjectOutputStream(client_socket.getOutputStream());
				oos.flush();
				ois = new ObjectInputStream(client_socket.getInputStream());

			} catch (Exception e) {
				AppendText("userService error");
			}
		}

		public UserService UserNum(String str) {
			int num = -1;
			UserService user = null;
			for (int i = 0; i < user_vc.size(); i++) {
				user = (UserService) user_vc.elementAt(i);

				if (user.UserName.matches(str)) {
					myNum = i;
					break;
				}

			}
			return user;
		}

		public synchronized void ConGame() {
			int n = UserNum(UserName).myNum;

			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user.UserStatus.matches("W") && !(user.UserName.matches(UserName))) {

					with = i;
					String msg = "[" + user.UserName + "]님과 게임 연결 ";
					user.LinkGame(msg);
					user.with = n;

					UserStatus = "G";
					user.UserStatus = "G";
					AppendText(UserName + " " + user.UserName + " 게임 연결");
					break;
				}

			}

		}

		public synchronized void Login() {
			AppendText("새로운 참가자 " + UserName + " 입장.");
			WriteOne("Welcome to Java chat server\n");
			WriteOne(UserName + "님 환영합니다.\n"); // 연결된 사용자에게 정상접속을 알림
			String msg = "[" + UserName + "]님이 입장 하였습니다.\n";
			UserStatus = "W"; // wait 상태
			WriteAllObject(msg); // 아직 user_vc에 새로 입장한 user는 포함되지 않았다.

		}

		public synchronized void Logout() {
			// String msg = "[" + UserName + "]님이 퇴장 하였습니다.\n";
			// Logout한 현재 객체를 벡터에서 지운다
			// WriteAll(msg); // 나를 제외한 다른 User들에게 전송
			if (UserStatus.matches("G")) {
				if (with != -1) {
					UserService user = (UserService) user_vc.elementAt(with);
					user.UserStatus = "W";
					user.with = -1;
					user.WriteOne(UserName + "님이 퇴장하였습니다\n");
				}
			}
			UserVec.removeElement(this);

			AppendText("사용자 " + "[" + UserName + "] 퇴장. 현재 참가자 수 " + UserVec.size());
		}

		public synchronized void WriteOne(String msg) {
			try {

				ChatMsg obcm = new ChatMsg("SERVER", "200", msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				Logout(); // 에러가난 현재 객체를 벡터에서 지운다
			}
		}

		public synchronized void LinkGame(String msg) {
			try {
				ChatMsg obcm = new ChatMsg(UserName, "150", msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout(); // 에러가난 현재 객체를 벡터에서 지운다
			}
		}

		public synchronized void WriteOthers(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				if (user != this && user.UserStatus.matches("W"))
					user.WriteOne(str);
			}
		}

		public synchronized void WriteAllObject(Object ob) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserService user = (UserService) user_vc.elementAt(i);
				// if (user.UserStatus.matches("W"))
				user.WriteOneObject(ob);
			}
		}

		public void WriteTo(Object ob) {
			UserService user = (UserService) user_vc.elementAt(with);
			user.WriteOneObject(ob);
			this.WriteOneObject(ob);
		}

		public synchronized void WriteOneObject(Object ob) {
			try {
				oos.writeObject(ob);
			} catch (IOException e) {
				AppendText("oos.writeObject(ob) error");
				try {
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout();
			}
		}

		public synchronized void Writemsg(String c, String msg) {
			try {
				ChatMsg obcm = new ChatMsg(UserName, c, msg);
				oos.writeObject(obcm);
			} catch (IOException e) {
				AppendText("dos.writeObject() error");
				try {
//				dos.close();
//				dis.close();
					ois.close();
					oos.close();
					client_socket.close();
					client_socket = null;
					ois = null;
					oos = null;
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Logout(); // 에러가난 현재 객체를 벡터에서 지운다
			}
		}

		public void run() {
			while (true) { // 사용자 접속을 계속해서 받기 위해 while문
				try {

//					String msg = new String(b, "euc-kr");
//					msg = msg.trim(); // 앞뒤 blank NULL, \n 모두 제거
					Object obcm = null;
					String msg = null;
					ChatMsg cm = null;
					// UserService user = (UserService) user_vc.elementAt(with);
					if (socket == null)
						break;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						return;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						// AppendObject(cm);
					} else
						continue;
					if (cm.getCode().matches("100")) {
						AppendObject(cm);
						UserName = cm.getId();

						Login();
						WriteAllObject(UserName + "님 입장");
						ConGame();

					} else if (cm.getCode().matches("150")) {
						AppendObject(cm);
						UserService user = (UserService) user_vc.elementAt(with);
						String m = "/ok ";
						user.LinkGame(m + UserName);
						this.LinkGame(m + user.UserName);
						this.WriteOne(user.UserName + "님과 게임 연결");
						user.WriteOne(UserName + "님과 게임 연결");

					} else if (cm.getCode().matches("200")) {
						AppendObject(cm);
						msg = String.format("[%s] %s", cm.getId(), cm.getData());
						AppendText(msg); // server 화면에 출력
						if (UserStatus.matches("G"))
							WriteTo(cm);
						else
							WriteAllObject(cm);

					} else if (cm.getCode().matches("400")) { // 타이머 스레드 시작
						AppendObject(cm);
						s++;
						UserService user = (UserService) user_vc.elementAt(with);
						user.Writemsg("415", cm.getData());
						if (s % 2 == 0 && s != 0) {

							dT[0] = new DeojiThread("D", 1000); // 일반 두더지 스레드
							dT[1] = new DeojiThread("N", 2500); // 감점두더지
							dT[2] = new DeojiThread("G", 3500); // +15두더지
							dT[3] = new DeojiThread("K", 4000); // 왕관 두더지

							Timer run = new Timer();
							Thread th = new Thread(run);
							for (int i = 0; i < 15; i++) {
								deoArr[i] = 0;
							}

							AppendText("==" + UserName + ", " + user.UserName + " 게임 시작==");
							th.start();
						}

					} else if (cm.getCode().matches("415")) { // 서버에 점수 저장, 상대에게 점수 전송
						score = Integer.parseInt(cm.getData());
						UserService user = (UserService) user_vc.elementAt(with);
						user.Writemsg("415", cm.getData());

					} else if (cm.getCode().matches("420")) { // 사라진 두더지 상태 변경
						int d = Integer.parseInt(cm.getData());
						deoArr[d] = 0;
					} else if (cm.getCode().matches("450")) { // 클릭한 두더지 위치 수신

						// String[] args = msg.split(" ");
						UserService user = (UserService) user_vc.elementAt(with);
						// System.out.println(cm.getData());
						user.Writemsg("450", cm.getData());

					} else if (cm.getCode().matches("600")) {// logout message 처리
						WriteAllObject(UserName + "님 퇴장");
						AppendObject(cm);
						Logout();
						break;
					}
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
//						dos.close();
//						dis.close();
						ois.close();
						oos.close();
						client_socket.close();
						for (int i = 0; i < 4; i++) {
							if (!dT[i].getStop()) {
								dT[0].threadStop(true);
								dT[1].threadStop(true);
								dT[2].threadStop(true);
								dT[3].threadStop(true);
							}
						}
						Logout(); // 에러가난 현재 객체를 벡터에서 지운다
						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝
			} // while
		} // run

		class DeojiThread extends Thread {
			private int index;
			private int preindex;
			private String msg;
			UserService user = (UserService) user_vc.elementAt(with);
			private boolean stop;
			private String who;
			private int delay;

			public DeojiThread(String who, int delay) {
				this.who = who;
				this.delay = delay;
				this.stop = false;
			}

			@Override
			public void run() {
				while (!stop) {
					while (true) {// 화면에 이미 올라와있는 곳 빼고

						index = (int) (Math.random() * 15);

						if (deoArr[index] == 0) {
							deoArr[index] = 1;
							break;
						}

					}

					msg = who + " " + Integer.toString(index);
					deoArr[index] = 1;

					Writemsg("420", msg);
					user.Writemsg("420", msg);
					preindex = index;
					if (time == 0) {
						return;
					}
					try {
						Thread.sleep(delay);
					} catch (InterruptedException e) {
						e.printStackTrace();
						return;
					}
				}

			}

			public void threadStop(boolean stop) {
				this.stop = stop;
			}

			public boolean getStop() {
				return stop;
			}
		}

		class Timer implements Runnable {
			UserService user = (UserService) user_vc.elementAt(with);

			public Timer() {

			}

			int t = 63;

			@Override
			public void run() {
				while (true) {

					if (t == 0) { // 게임 종료
						String m = Integer.toString(t);
						Writemsg("500", m);
						user.Writemsg("500", m);

						dT[0].threadStop(true);
						dT[1].threadStop(true);
						dT[2].threadStop(true);
						dT[3].threadStop(true);
						AppendText("==" + UserName + ", " + user.UserName + " 게임 종료==");
						return;
					}
					if (t > 60) {// 시작 전 3,2,1 카운트
						String m = "/s " + (t - 60);
						Writemsg("410", m);
						user.Writemsg("410", m);
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							return;
						}
						time = t;
						t--;

					}

					else { // 게임 60초 카운트
						if (t == 60) {
							dT[0].start();
						} else if (t == 50) {
							dT[1].start();
						} else if (t == 35) {
							dT[2].start();
						} else if (t == 25) {
							dT[3].start();
						}

						String m = "/g " + t;
						Writemsg("410", m);
						user.Writemsg("410", m);

						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							return;
						}
						t--;
					}

				}
			}
		}

	}

}
