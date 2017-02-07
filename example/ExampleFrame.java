import com.wjblack.FormLayout;
import javax.swing.*;

public class ExampleFrame {
	public static void main(String argv[]) {
		JFrame frm = new JFrame("Test");
		frm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel form = new JPanel();
		FormLayout layout = new FormLayout(form);
		form.setLayout(layout);
		form.add("Name", new JTextField());
		form.add("Address", new JTextField());
		form.add("City/State/ZIP", new JTextField());
		form.add("Interests", new JTextField());
		form.add("Special Offers?", new JTextField());
		form.add(FormLayout.BUTTONAREA, new JButton("Cancel"));
		form.add(FormLayout.BUTTONAREA, new JButton("Save"));
		frm.setContentPane(form);
		frm.pack();
		frm.setVisible(true);
	}
}
