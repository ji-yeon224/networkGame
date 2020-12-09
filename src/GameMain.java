import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.DatagramSocket;
import java.net.Socket;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;

public class GameMain extends JFrame {

	private JPanel panel;
	private JPanel contentPane;
	private JLabel lblTime;
	private JLabel lblNewLabel;
	private JLabel lblGameWith;
	private JLabel lblTimeNum;
	private JLabel lblScoreNum;
	private JLabel userScore;
	private JLabel withScore;
	private JLabel[] addScore;

	private JTextField txtInput;
	private JScrollPane scrollPane;
	private JButton btnSend;
	private JTextPane textArea;

	private JLabel[] deo; // 일반두더지
	private JLabel[] Ndeo; // 감점두더지
	private JLabel[] deoT; // 잡은 두더지
	private JLabel[] Gdeo;
	private JLabel[] Kdeo;

	private String UserName;
	private int RoomNum;
	private static final int BUF_LEN = 128; // Windows 처럼 BUF_LEN 을 정의
	private Socket socket; // 연결소켓
	private InputStream is;
	private OutputStream os;
	private DataInputStream dis;
	private DataOutputStream dos;

	private ObjectInputStream ois;
	private ObjectOutputStream oos;

	private DatagramSocket udp_socket; // 연결소켓
	private int with;
	private JLabel lblSCount;
	private int score = 0;

	private ImageIcon Ndeoji;
	private ImageIcon deoji;
	private ImageIcon Gdeoji;
	private ImageIcon Kdeoji;
	private ImageIcon NdeojiT;
	private ImageIcon deojiT;
	private ImageIcon GdeojiT;
	private ImageIcon KdeojiT;

	private DeojiThread[] deojiThread = new DeojiThread[15];
	private CatchThread[] catchThread = new CatchThread[15];

	private JPanel overPanel;
	private JButton btnExit1;
	private JButton btnAgain;
	private JLabel lblwin_lose;
	
	private boolean flag = false;
	
	/**
	 * Launch the application.
	 */
	/*
	 * public static void main(String[] args) { EventQueue.invokeLater(new
	 * Runnable() { public void run() { try { GameMain frame = new
	 * GameMain(username, ip_addr, port_no); frame.setVisible(true); } catch
	 * (Exception e) { e.printStackTrace(); } } }); }
	 */

	/**
	 * Create the frame.
	 */
	public GameMain(String username, String ip_addr, String port_no) {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 953, 546);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(51, 51, 51));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		
		panel = new JPanel() {
			ImageIcon img = new ImageIcon("src/pan.png");

			public void paintComponent(Graphics g) {
				g.drawImage(img.getImage(), 0, 0, null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};
		panel.setBounds(23, 88, 600, 396);
		contentPane.setLayout(null);
		contentPane.add(panel);
		panel.setLayout(null);

		lblSCount = new JLabel("");
		lblSCount.setForeground(new Color(255, 102, 51));
		lblSCount.setFont(new Font("Franklin Gothic Demi Cond", Font.PLAIN, 99));
		lblSCount.setBounds(270, 115, 79, 129);
		// lblSCount.setVisible(false);
		panel.add(lblSCount);

		overPanel = new JPanel();
		overPanel.setBackground(new Color(51, 51, 51));
		overPanel.setBounds(115, 82, 375, 217);
		panel.add(overPanel);
		overPanel.setLayout(null);

		JLabel lblNewLabel_1 = new JLabel("G A M E  O V E R");
		lblNewLabel_1.setForeground(new Color(204, 51, 51));
		lblNewLabel_1.setFont(new Font("Franklin Gothic Demi Cond", Font.PLAIN, 58));
		lblNewLabel_1.setBounds(12, 0, 350, 93);
		overPanel.add(lblNewLabel_1);

		btnExit1 = new JButton("E X I T");
		btnExit1.setForeground(new Color(204, 204, 204));
		btnExit1.setBackground(new Color(153, 153, 204));
		btnExit1.setFont(new Font("Franklin Gothic Demi Cond", Font.PLAIN, 15));
		btnExit1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SendContent("600","Bye");
				System.exit(0);
			}
		});
		btnExit1.setBounds(234, 171, 91, 36);
		overPanel.add(btnExit1);

		btnAgain = new JButton("AGAIN");
		btnAgain.setForeground(new Color(204, 204, 204));
		btnAgain.setFont(new Font("Franklin Gothic Demi Cond", Font.PLAIN, 15));
		btnAgain.setBackground(new Color(153, 153, 204));
		btnAgain.setBounds(64, 171, 91, 36);
		btnAgain.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				overPanel.setVisible(false);
			}
		});
		overPanel.add(btnAgain);

		lblwin_lose = new JLabel("");
		lblwin_lose.setForeground(new Color(255, 255, 255));
		lblwin_lose.setFont(new Font("Franklin Gothic Demi Cond", Font.BOLD, 35));
		lblwin_lose.setBounds(137, 103, 163, 36);
		overPanel.add(lblwin_lose);

		overPanel.setVisible(false);

		// 두더지
		MyMouseListener m = new MyMouseListener();
		deoji = new ImageIcon("src/deoji2.png");

		Ndeoji = new ImageIcon("src/Ndeoji.png");
		Gdeoji = new ImageIcon("src/Gdeoji11.png");
		Kdeoji = new ImageIcon("src/Kdeoji11.png");

		NdeojiT = new ImageIcon("src/NdeojiT.png");
		deojiT = new ImageIcon("src/deojiT.png");
		GdeojiT = new ImageIcon("src/GdeojiT10.png");
		KdeojiT = new ImageIcon("src/KdeojiT11.png");

		Ndeo = new JLabel[15];
		deo = new JLabel[15];
		deoT = new JLabel[15];
		Gdeo = new JLabel[15];
		Kdeo = new JLabel[15];

		for (int i = 0; i < 15; i++) {
			deo[i] = new JLabel(deoji);
			deo[i].addMouseListener(m);
			deo[i].setName("D " + Integer.toString(i));

			Ndeo[i] = new JLabel(Ndeoji);
			Ndeo[i].addMouseListener(m);
			Ndeo[i].setName("N " + Integer.toString(i));

			Gdeo[i] = new JLabel(Gdeoji);
			Gdeo[i].addMouseListener(m);
			Gdeo[i].setName("G " + Integer.toString(i));

			Kdeo[i] = new JLabel(Kdeoji);
			Kdeo[i].addMouseListener(m);
			Kdeo[i].setName("K " + Integer.toString(i));

			deoT[i] = new JLabel();
			deoT[i].setName(Integer.toString(i));
		}

		addScore = new JLabel[15];
		for (int i = 0; i < 15; i++) {
			addScore[i] = new JLabel("");
			addScore[i].setName(Integer.toString(i));
			addScore[i].setFont(new Font("Franklin Gothic Demi", Font.BOLD, 28));
			addScore[i].setForeground(new Color(255, 102, 51));

		}

		int x = 18, y = 55;
		for (int i = 0; i < 15; i++) {

			if (i % 5 == 0 && i != 0) {
				y += 113;
				x = 18;
			}
			addScore[i].setBounds(x + 10, y - 30, 70, 20);
			deo[i].setBounds(x, y, 70, 81);
			Ndeo[i].setBounds(x, y, 70, 81);
			Gdeo[i].setBounds(x, y - 14, 70, 100);
			Kdeo[i].setBounds(x, y - 14, 70, 100);
			deoT[i].setBounds(x, y - 10, 70, 94);
			if (i % 5 == 1)
				x += 122;
			else
				x += 123;

			panel.add(addScore[i]);
			panel.add(deo[i]);
			panel.add(Ndeo[i]);
			panel.add(Gdeo[i]);
			panel.add(Kdeo[i]);
			panel.add(deoT[i]);

			addScore[i].setVisible(false);
			deo[i].setVisible(false);
			Gdeo[i].setVisible(false);
			Kdeo[i].setVisible(false);
			Ndeo[i].setVisible(false);
			deoT[i].setVisible(false);
		}

		lblTime = new JLabel("TIME: ");
		lblTime.setFont(new Font("Franklin Gothic Demi", Font.BOLD, 28));
		lblTime.setForeground(new Color(153, 204, 51));
		lblTime.setBounds(23, 34, 87, 44);
		contentPane.add(lblTime);

		lblNewLabel = new JLabel("SCORE: ");
		lblNewLabel.setForeground(new Color(153, 204, 51));
		lblNewLabel.setFont(new Font("Franklin Gothic Demi", Font.BOLD, 28));
		lblNewLabel.setBounds(287, 40, 118, 33);
		contentPane.add(lblNewLabel);

		txtInput = new JTextField();
		txtInput.setBounds(646, 451, 188, 33);
		contentPane.add(txtInput);
		txtInput.setColumns(10);

		scrollPane = new JScrollPane();
		scrollPane.setBounds(646, 179, 252, 262);
		contentPane.add(scrollPane);

		textArea = new JTextPane();
		textArea.setEditable(true);
		textArea.setFont(new Font("굴림체", Font.PLAIN, 12));
		scrollPane.setViewportView(textArea);

		btnSend = new JButton("SEND");
		btnSend.setFont(new Font("굴림", Font.PLAIN, 10));
		btnSend.setBounds(835, 451, 63, 33);
		contentPane.add(btnSend);

		JLabel lblUsername = new JLabel("Name");
		lblUsername.setFont(new Font("Franklin Gothic Demi Cond", Font.PLAIN, 17));
		lblUsername.setForeground(Color.PINK);
		lblUsername.setBounds(23, 9, 87, 26);
		contentPane.add(lblUsername);
		setVisible(true);

		UserName = username;
		lblUsername.setText(username);

		JLabel lblMyName = new JLabel("Name");
		lblMyName.setForeground(Color.PINK);
		lblMyName.setFont(new Font("Franklin Gothic Demi Cond", Font.PLAIN, 20));
		lblMyName.setBounds(658, 43, 81, 33);
		lblMyName.setText(username);
		contentPane.add(lblMyName);

		lblGameWith = new JLabel("");
		lblGameWith.setForeground(new Color(0, 204, 255));
		lblGameWith.setFont(new Font("Franklin Gothic Demi Cond", Font.PLAIN, 20));
		lblGameWith.setBounds(658, 130, 81, 33);
		contentPane.add(lblGameWith);
		setVisible(true);

		JLabel lblVS = new JLabel("VS");
		lblVS.setFont(new Font("Franklin Gothic Demi Cond", Font.ITALIC, 17));
		lblVS.setForeground(new Color(255, 255, 255));
		lblVS.setBounds(658, 101, 50, 15);
		contentPane.add(lblVS);

		lblTimeNum = new JLabel("60");
		lblTimeNum.setForeground(new Color(255, 153, 51));
		lblTimeNum.setFont(new Font("Franklin Gothic Demi", Font.BOLD, 28));
		lblTimeNum.setBounds(126, 34, 87, 44);
		contentPane.add(lblTimeNum);

		lblScoreNum = new JLabel("0");
		lblScoreNum.setForeground(new Color(153, 255, 255));
		lblScoreNum.setFont(new Font("Franklin Gothic Demi", Font.BOLD, 28));
		lblScoreNum.setBounds(417, 34, 87, 44);
		contentPane.add(lblScoreNum);

		JButton btnStart = new JButton("START");
		btnStart.setForeground(new Color(255, 255, 255));
		btnStart.setBackground(new Color(204, 102, 102));
		btnStart.setFont(new Font("Franklin Gothic Demi Cond", Font.PLAIN, 18));
		btnStart.setBounds(516, 34, 91, 44);
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				score = 0;
				lblScoreNum.setText("0");
				SendContent("400", "Ready");
				userScore.setText("Ready");

			}
		});
		contentPane.add(btnStart);

		userScore = new JLabel("");
		userScore.setForeground(new Color(255, 255, 51));
		userScore.setFont(new Font("Franklin Gothic Demi Cond", Font.PLAIN, 20));
		userScore.setBounds(771, 57, 63, 15);
		contentPane.add(userScore);

		withScore = new JLabel("");
		withScore.setForeground(new Color(255, 255, 51));
		withScore.setFont(new Font("Franklin Gothic Demi Cond", Font.PLAIN, 20));
		withScore.setBounds(771, 133, 63, 15);
		contentPane.add(withScore);

		JButton btnExit = new JButton("E X I T");
		btnExit.setForeground(new Color(255, 255, 255));
		btnExit.setBackground(new Color(102, 153, 0));
		btnExit.setFont(new Font("Arial Rounded MT Bold", Font.BOLD, 16));
		btnExit.setBounds(835, 10, 92, 25);
		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				SendContent("600","Bye");
				if(flag) {
					for(int i=0;i<15;i++) {
						if(!deojiThread[i].getStop()) {
							deojiThread[i].threadStop(true);
						}
					}
				}
				
				System.exit(0);

			}
		});
		contentPane.add(btnExit);
		
		

		try {
			socket = new Socket(ip_addr, Integer.parseInt(port_no));

			udp_socket = new DatagramSocket();

			oos = new ObjectOutputStream(socket.getOutputStream());
			oos.flush();
			ois = new ObjectInputStream(socket.getInputStream());

			// SendMessage("/login " + UserName);
			
			ChatMsg obcm = new ChatMsg(UserName, "100", "Hello");
			SendObject(obcm);
			
			ListenNetwork net = new ListenNetwork();
			net.start();
			TextSendAction action = new TextSendAction();
			btnSend.addActionListener(action);
			txtInput.addActionListener(action);
			txtInput.requestFocus();

		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			AppendText("connect error");
		}

	}

	class MyMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {

			JLabel d = (JLabel) e.getSource();
			String dName = d.getName();
			String[] args = dName.split(" "); // args[0]: 두더지 종류 / args[1]: 위치
			int idx = Integer.parseInt(args[1]);
			String dN;

			d.setVisible(false);
			addScore[idx].setForeground(Color.PINK);
			if (args[0].matches("D")) {
				addScore[idx].setText("+10");
				score += 10;

				dN = "D " + d.getName();
				deoT[idx].setIcon(deojiT);
				catchThread[idx] = new CatchThread("D", idx);
			} else if (args[0].matches("N")) {
				addScore[idx].setText("-5");
				score -= 5;

				dN = "N " + d.getName();
				deoT[idx].setIcon(NdeojiT);
				catchThread[idx] = new CatchThread("N", idx);
			} else if (args[0].matches("G")) {
				addScore[idx].setText("+15");
				score += 15;
				dN = "G " + d.getName();
				deoT[idx].setIcon(GdeojiT);
				catchThread[idx] = new CatchThread("G", idx);
			} else if (args[0].matches("K")) {
				addScore[idx].setText("+25");
				score += 25;

				dN = "K " + d.getName();
				deoT[idx].setIcon(KdeojiT);
				catchThread[idx] = new CatchThread("K", idx);
			}

//			int idx = Integer.parseInt(d.getName());

			addScore[idx].setVisible(true);

			userScore.setText(Integer.toString(score));
			lblScoreNum.setText(Integer.toString(score));

			SendContent("415", Integer.toString(score)); // 점수 전송
			SendContent("450", dName);// 서버에 두더지 위치 전송
			SendContent("420", args[1]);
			deojiThread[idx].threadStop(true);

			deoT[idx].setVisible(true);

			catchThread[idx].start();
		}

		@Override
		public void mousePressed(MouseEvent e) {
		}

		@Override
		public void mouseReleased(MouseEvent e) {
			JLabel d = (JLabel) e.getSource();
			String dName = d.getName();
			String[] args = dName.split(" "); // args[0]: 두더지 종류 / args[1]: 위치
			int idx = Integer.parseInt(args[1]);
			String dN;

			d.setVisible(false);
			addScore[idx].setForeground(Color.PINK);
			if (args[0].matches("D")) {
				addScore[idx].setText("+10");
				score += 10;

				dN = "D " + d.getName();
				deoT[idx].setIcon(deojiT);
				catchThread[idx] = new CatchThread("D", idx);
			} else if (args[0].matches("N")) {
				addScore[idx].setText("-5");
				score -= 5;

				dN = "N " + d.getName();
				deoT[idx].setIcon(NdeojiT);
				catchThread[idx] = new CatchThread("N", idx);
			} else if (args[0].matches("G")) {
				addScore[idx].setText("+15");
				score += 15;
				dN = "G " + d.getName();
				deoT[idx].setIcon(GdeojiT);
				catchThread[idx] = new CatchThread("G", idx);
			} else if (args[0].matches("K")) {
				addScore[idx].setText("+25");
				score += 25;

				dN = "K " + d.getName();
				deoT[idx].setIcon(KdeojiT);
				catchThread[idx] = new CatchThread("K", idx);
			}

//			int idx = Integer.parseInt(d.getName());

			addScore[idx].setVisible(true);

			userScore.setText(Integer.toString(score));
			lblScoreNum.setText(Integer.toString(score));

			SendContent("415", Integer.toString(score)); // 점수 전송
			SendContent("450", dName);// 서버에 두더지 위치 전송
			SendContent("420", args[1]);
			deojiThread[idx].threadStop(true);

			deoT[idx].setVisible(true);

			catchThread[idx].start();
		}

		@Override
		public void mouseExited(MouseEvent e) {
		}

		@Override
		public void mouseEntered(MouseEvent e) {
		}

	}
	

	
	// Server Message를 수신해서 화면에 표시
	class ListenNetwork extends Thread {
		public void run() {
			while (true) {

				try {

					Object obcm = null;
					String msg = null;
					ChatMsg cm;
					try {
						obcm = ois.readObject();
					} catch (ClassNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						break;
					}
					if (obcm == null)
						break;
					if (obcm instanceof ChatMsg) {
						cm = (ChatMsg) obcm;
						// with = cm.getWith();
						msg = String.format("[%s] %s", cm.getId(), cm.getData());
					} else
						continue;
					String[] args = msg.split(" "); // 단어들을 분리한다.
					switch (cm.getCode()) {

					case "150":
						// with = cm.getWith();

						if (args[1].matches("/ok")) {
							// System.out.println(cm.getData() + " " + UserName);
							String[] a = cm.getData().split(" ");
							lblGameWith.setText(a[1]);

						}

						else
							SendContent("150", "연결");
						break;
					case "200": // chat message
						// with = cm.getWith();
						AppendText(msg);
						// deo[1].setVisible(true);
						break;
					case "410":
						if (args[1].matches("/s")) {
							userScore.setText("0");
							withScore.setText("0");
							lblSCount.setVisible(true);
							lblSCount.setText(args[2]);
							flag = true;
						} else if (args[1].matches("/g")) {

							lblSCount.setVisible(false);
							lblTimeNum.setText(args[2]);

						}
						break;
					case "415":
						withScore.setText(cm.getData());
						break;
					case "420":
						int d = Integer.parseInt(args[2]);
						if (args[1].matches("N"))
							Ndeo[d].setVisible(true);
						else if (args[1].matches("D")) {
							deo[d].setVisible(true);
						} else if (args[1].matches("G")) {
							Gdeo[d].setVisible(true);
						} else if (args[1].matches("K")) {
							Kdeo[d].setVisible(true);
						}
						deojiThread[d] = new DeojiThread(args[1], d);
						deojiThread[d].start();

						// System.out.println(msg + " client");
						break;

					case "450":

						int w = Integer.parseInt(args[2]);
						if (args[1].matches("D")) {
							deo[w].setVisible(false);
							addScore[w].setForeground(new Color(0, 204, 255));
							addScore[w].setText("+10");
							addScore[w].setVisible(true);

							deojiThread[w].threadStop(true);
							deoT[w].setIcon(deojiT);
							deoT[w].setVisible(true);
							catchThread[w] = new CatchThread(args[1], w);
							catchThread[w].start();
						}

						else if (args[1].matches("N")) {
							Ndeo[w].setVisible(false);
							addScore[w].setForeground(new Color(0, 204, 255));
							addScore[w].setText("-5");
							addScore[w].setVisible(true);

							deojiThread[w].threadStop(true);
							deoT[w].setIcon(NdeojiT);
							deoT[w].setVisible(true);
							catchThread[w] = new CatchThread(args[1], w);
							catchThread[w].start();
						}

						else if (args[1].matches("G")) {
							Gdeo[w].setVisible(false);
							addScore[w].setForeground(new Color(0, 204, 255));
							addScore[w].setText("+15");
							addScore[w].setVisible(true);

							deojiThread[w].threadStop(true);
							deoT[w].setIcon(GdeojiT);
							deoT[w].setVisible(true);
							catchThread[w] = new CatchThread(args[1], w);
							catchThread[w].start();
						} 
						else if (args[1].matches("K")) {
							Kdeo[w].setVisible(false);
							addScore[w].setForeground(new Color(0, 204, 255));
							addScore[w].setText("+25");
							addScore[w].setVisible(true);

							deojiThread[w].threadStop(true);
							deoT[w].setIcon(KdeojiT);
							deoT[w].setVisible(true);
							catchThread[w] = new CatchThread(args[1], w);
							catchThread[w].start();
						}

						break;
					case "500":
						lblTimeNum.setText(args[1]);
						for (int i = 0; i < 15; i++) {
							deojiThread[i].threadStop(true);
							deo[i].setVisible(false);
							Ndeo[i].setVisible(false);
							Gdeo[i].setVisible(false);
							Kdeo[i].setVisible(false);
						}
						int ws = Integer.parseInt(withScore.getText());
						if (score > ws) {
							lblwin_lose.setText("  W I N");
						} else if (score < ws) {
							lblwin_lose.setText(" L O S E");
						} else {
							lblwin_lose.setText("  T I E");
						}
						overPanel.setVisible(true);
						flag = false;
						break;
					}
				} catch (IOException e) {
					AppendText("ois.readObject() error");
					try {
//							dos.close();
//							dis.close();
						ois.close();
						oos.close();
						socket.close();

						break;
					} catch (Exception ee) {
						break;
					} // catch문 끝
				} // 바깥 catch문끝

			}
		}
	}

	class DeojiThread extends Thread {
		private String who;
		private int index;
		private boolean stop;

		public DeojiThread(String who, int index) {
			this.who = who;
			this.index = index;
			this.stop = false;
		}

		@Override
		public void run() {
			int t = 0;
			while (!stop) {
				try {
					t++;
					if (t == 2) {

						if (who.matches("N"))
							Ndeo[index].setVisible(false);
						else if(who.matches("D"))
							deo[index].setVisible(false);
						else if(who.matches("G"))
							Gdeo[index].setVisible(false);
						else if(who.matches("K"))
							Kdeo[index].setVisible(false);
						String m = Integer.toString(index);
						SendContent("420", m);
						return;
					}
					Thread.sleep(1500);

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

	class CatchThread extends Thread {
		private String who;
		private int index;
		public CatchThread(String who, int index) {
			this.who = who;
			this.index = index;
		}

		@Override
		public void run() {

			try {
				Thread.sleep(500);
				deoT[index].setVisible(false);
				addScore[index].setVisible(false);
				SendContent("420", Integer.toString(index));
				return;

			} catch (InterruptedException e) {
				e.printStackTrace();
				return;
			}

		}
		
	}

	// keyboard enter key 치면 서버로 전송
	class TextSendAction implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			// Send button을 누르거나 메시지 입력하고 Enter key 치면
			if (e.getSource() == btnSend || e.getSource() == txtInput) {
				String msg = null;
				// msg = String.format("[%s] %s\n", UserName, txtInput.getText());
				msg = txtInput.getText();
				SendContent("200", msg);
				txtInput.setText(""); // 메세지를 보내고 나면 메세지 쓰는창을 비운다.
				txtInput.requestFocus(); // 메세지를 보내고 커서를 다시 텍스트 필드로 위치시킨다
				if (msg.contains("/exit")) // 종료 처리
					System.exit(0);
			}
		}
	}

	// 화면에 출력
	public synchronized void AppendText(String msg) {
		// textArea.append(msg + "\n");
		// AppendIcon(icon1);
		msg = msg.trim(); // 앞뒤 blank와 \n을 제거한다.
		int len = textArea.getDocument().getLength();
		// 끝으로 이동
		textArea.setCaretPosition(len);
		textArea.replaceSelection(msg + "\n");

	}

	// Windows 처럼 message 제외한 나머지 부분은 NULL 로 만들기 위한 함수
	public byte[] MakePacket(String msg) {
		byte[] packet = new byte[BUF_LEN];
		byte[] bb = null;
		int i;
		for (i = 0; i < BUF_LEN; i++)
			packet[i] = 0;
		try {
			bb = msg.getBytes("euc-kr");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(0);
		}
		for (i = 0; i < bb.length; i++)
			packet[i] = bb[i];
		return packet;
	}

	public synchronized void SendContent(String code, String msg) {
		try {

			ChatMsg obcm = new ChatMsg(UserName, code, msg);

			oos.writeObject(obcm);
		} catch (IOException e) {

			AppendText("oos.writeObject() error");
			try {
				ois.close();
				oos.close();
				socket.close();
			} catch (IOException e1) {
				e1.printStackTrace();
				System.exit(0);
			}
		}
	}

	public synchronized void SendObject(Object ob) { // 서버로 메세지를 보내는 메소드
		try {
			oos.writeObject(ob);
		} catch (IOException e) {
			AppendText("SendObject Error");
		}
	}
}
