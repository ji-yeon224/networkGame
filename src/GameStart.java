import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.awt.Dimension;

import net.miginfocom.swing.MigLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.JLabel;
import javax.swing.JButton;
import java.awt.Font;

public class GameStart extends JFrame {

	private JPanel contentPane;
	private JTextField txtUserName;
	private JTextField txtIpAddress;
	private JTextField txtPort;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GameStart frame = new GameStart();
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
	public GameStart() {
		setBackground(new Color(51, 51, 51));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 375, 557);
		contentPane = new JPanel();
		contentPane.setBackground(new Color(51, 51, 51));
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JPanel panel = new JPanel() {
			ImageIcon img = new ImageIcon("src/logo1.png");
			public void paintComponent(Graphics g) {
				g.drawImage(img.getImage(), 0,0,null);
				setOpaque(false);
				super.paintComponent(g);
			}
		};
		panel.setBounds(33, 50, 320, 164);
		//Dimension d = panel.getSize();
		//ImageIcon img = new ImageIcon("logo.png");
		
		contentPane.add(panel);
		
		
		JLabel lblUserName = new JLabel("User Name");
		lblUserName.setFont(new Font("굴림", Font.BOLD, 14));
		lblUserName.setForeground(new Color(255, 153, 51));
		lblUserName.setBounds(33, 258, 113, 15);
		
		contentPane.add(lblUserName);
		
		txtUserName = new JTextField();
		txtUserName.setBounds(114, 248, 180, 36);
		txtUserName.setColumns(10);		
		contentPane.add(txtUserName);
		
		JLabel lblIpAddress = new JLabel("IP Address");
		lblIpAddress.setForeground(new Color(255, 255, 255));
		lblIpAddress.setBounds(33, 322, 86, 15);
		contentPane.add(lblIpAddress);
		
		txtIpAddress = new JTextField();
		txtIpAddress.setBounds(114, 312, 180, 36);
		txtIpAddress.setText("127.0.0.1");
		txtIpAddress.setColumns(10);		
		contentPane.add(txtIpAddress);
		
		JLabel lblPort = new JLabel("Port Number");
		lblPort.setForeground(new Color(255, 255, 255));
		lblPort.setBounds(33, 397, 86, 15);
		contentPane.add(lblPort);
		
		txtPort = new JTextField();
		txtPort.setBounds(114, 376, 180, 36);
		txtPort.setText("30000");
		txtPort.setColumns(10);
		contentPane.add(txtPort);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.setBounds(128, 442, 113, 42);
		contentPane.add(btnConnect);
		Myaction action = new Myaction();
		btnConnect.addActionListener(action);
		txtUserName.addActionListener(action);
		txtIpAddress.addActionListener(action);
		txtPort.addActionListener(action);
		
	}
	class Myaction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		@Override
		public void actionPerformed(ActionEvent e) {
			String username = txtUserName.getText().trim();
			String ip_addr = txtIpAddress.getText().trim();
			String port_no = txtPort.getText().trim();
			GameMain view = new GameMain(username, ip_addr, port_no);
			
			setVisible(false);
		}
	}
}
