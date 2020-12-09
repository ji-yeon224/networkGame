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

	private ServerSocket socket; // ��������
	private Socket client_socket; // accept() ���� ������ client ����
	private Vector UserVec = new Vector(); // ����� ����ڸ� ������ ����
	private static final int BUF_LEN = 128; // Windows ó�� BUF_LEN �� ����
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
				btnServerStart.setEnabled(false); // ������ ���̻� �����Ű�� �� �ϰ� ���´�
				txtPortNumber.setEnabled(false); // ���̻� ��Ʈ��ȣ ������ �ϰ� ���´�
				AcceptServer accept_server = new AcceptServer();
				accept_server.start();
			}
		});
		btnServerStart.setBounds(75, 478, 217, 41);
		contentPane.add(btnServerStart);

	}

	// ���ο� ������ accept() �ϰ� user thread�� ���� �����Ѵ�.
	class AcceptServer extends Thread {
		@SuppressWarnings("unchecked")
		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				try {
					AppendText("Waiting new clients ...");
					client_socket = socket.accept(); // accept�� �Ͼ�� �������� ���� �����
					AppendText("���ο� ������ from " + client_socket);
					// User �� �ϳ��� Thread ����
					UserService new_user = new UserService(client_socket);
					UserVec.add(new_user); // ���ο� ������ �迭�� �߰�
					new_user.start(); // ���� ��ü�� ������ ����
					AppendText("���� ������ �� " + UserVec.size());
				} catch (IOException e) {
					AppendText("accept() error");
					// System.exit(0);
				}
			}
		}
	}

	public synchronized void AppendText(String str) {
		// textArea.append("����ڷκ��� ���� �޼��� : " + str+"\n");
		textArea.append(str + "\n");
		textArea.setCaretPosition(textArea.getText().length());
	}

	public synchronized void AppendObject(ChatMsg msg) {
		// textArea.append("����ڷκ��� ���� object : " + str+"\n");
		textArea.append("[" + msg.getId() + "]" + " " + msg.getCode() + "\n");
		textArea.append("data = " + msg.getData() + "\n");

		textArea.setCaretPosition(textArea.getText().length());
	}

	// User �� �����Ǵ� Thread
	// Read One ���� ��� -> Write All
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
			// �Ű������� �Ѿ�� �ڷ� ����
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
					String msg = "[" + user.UserName + "]�԰� ���� ���� ";
					user.LinkGame(msg);
					user.with = n;

					UserStatus = "G";
					user.UserStatus = "G";
					AppendText(UserName + " " + user.UserName + " ���� ����");
					break;
				}

			}

		}

		public synchronized void Login() {
			AppendText("���ο� ������ " + UserName + " ����.");
			WriteOne("Welcome to Java chat server\n");
			WriteOne(UserName + "�� ȯ���մϴ�.\n"); // ����� ����ڿ��� ���������� �˸�
			String msg = "[" + UserName + "]���� ���� �Ͽ����ϴ�.\n";
			UserStatus = "W"; // wait ����
			WriteAllObject(msg); // ���� user_vc�� ���� ������ user�� ���Ե��� �ʾҴ�.

		}

		public synchronized void Logout() {
			// String msg = "[" + UserName + "]���� ���� �Ͽ����ϴ�.\n";
			// Logout�� ���� ��ü�� ���Ϳ��� �����
			// WriteAll(msg); // ���� ������ �ٸ� User�鿡�� ����
			if (UserStatus.matches("G")) {
				if (with != -1) {
					UserService user = (UserService) user_vc.elementAt(with);
					user.UserStatus = "W";
					user.with = -1;
					user.WriteOne(UserName + "���� �����Ͽ����ϴ�\n");
				}
			}
			UserVec.removeElement(this);

			AppendText("����� " + "[" + UserName + "] ����. ���� ������ �� " + UserVec.size());
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
				Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
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
				Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
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
				Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
			}
		}

		public void run() {
			while (true) { // ����� ������ ����ؼ� �ޱ� ���� while��
				try {

//					String msg = new String(b, "euc-kr");
//					msg = msg.trim(); // �յ� blank NULL, \n ��� ����
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
						WriteAllObject(UserName + "�� ����");
						ConGame();

					} else if (cm.getCode().matches("150")) {
						AppendObject(cm);
						UserService user = (UserService) user_vc.elementAt(with);
						String m = "/ok ";
						user.LinkGame(m + UserName);
						this.LinkGame(m + user.UserName);
						this.WriteOne(user.UserName + "�԰� ���� ����");
						user.WriteOne(UserName + "�԰� ���� ����");

					} else if (cm.getCode().matches("200")) {
						AppendObject(cm);
						msg = String.format("[%s] %s", cm.getId(), cm.getData());
						AppendText(msg); // server ȭ�鿡 ���
						if (UserStatus.matches("G"))
							WriteTo(cm);
						else
							WriteAllObject(cm);

					} else if (cm.getCode().matches("400")) { // Ÿ�̸� ������ ����
						AppendObject(cm);
						s++;
						UserService user = (UserService) user_vc.elementAt(with);
						user.Writemsg("415", cm.getData());
						if (s % 2 == 0 && s != 0) {

							dT[0] = new DeojiThread("D", 1000); // �Ϲ� �δ��� ������
							dT[1] = new DeojiThread("N", 2500); // �����δ���
							dT[2] = new DeojiThread("G", 3500); // +15�δ���
							dT[3] = new DeojiThread("K", 4000); // �հ� �δ���

							Timer run = new Timer();
							Thread th = new Thread(run);
							for (int i = 0; i < 15; i++) {
								deoArr[i] = 0;
							}

							AppendText("==" + UserName + ", " + user.UserName + " ���� ����==");
							th.start();
						}

					} else if (cm.getCode().matches("415")) { // ������ ���� ����, ��뿡�� ���� ����
						score = Integer.parseInt(cm.getData());
						UserService user = (UserService) user_vc.elementAt(with);
						user.Writemsg("415", cm.getData());

					} else if (cm.getCode().matches("420")) { // ����� �δ��� ���� ����
						int d = Integer.parseInt(cm.getData());
						deoArr[d] = 0;
					} else if (cm.getCode().matches("450")) { // Ŭ���� �δ��� ��ġ ����

						// String[] args = msg.split(" ");
						UserService user = (UserService) user_vc.elementAt(with);
						// System.out.println(cm.getData());
						user.Writemsg("450", cm.getData());

					} else if (cm.getCode().matches("600")) {// logout message ó��
						WriteAllObject(UserName + "�� ����");
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
						Logout(); // �������� ���� ��ü�� ���Ϳ��� �����
						break;
					} catch (Exception ee) {
						break;
					} // catch�� ��
				} // �ٱ� catch����
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
					while (true) {// ȭ�鿡 �̹� �ö���ִ� �� ����

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

					if (t == 0) { // ���� ����
						String m = Integer.toString(t);
						Writemsg("500", m);
						user.Writemsg("500", m);

						dT[0].threadStop(true);
						dT[1].threadStop(true);
						dT[2].threadStop(true);
						dT[3].threadStop(true);
						AppendText("==" + UserName + ", " + user.UserName + " ���� ����==");
						return;
					}
					if (t > 60) {// ���� �� 3,2,1 ī��Ʈ
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

					else { // ���� 60�� ī��Ʈ
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
