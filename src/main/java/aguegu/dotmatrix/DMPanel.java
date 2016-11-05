package aguegu.dotmatrix;

import javax.swing.JPanel;
import java.awt.event.MouseListener;
import java.awt.event.MouseEvent;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseMotionListener;

class DMPanel extends JPanel implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = -2531292225634588108L;
	private DotMatrix dm;
	private DMImage[] dmi;
	private DMMode mode;

	public DMPanel() {
		this.setSize(DMImage.getBlockWidth() * (9 * 8 + 1),
				DMImage.getBlockWidth() * (9 * 3 + 1));

		this.setPreferredSize(new Dimension(DMImage.getBlockWidth()
				* (9 * 8 + 1), DMImage.getBlockWidth() * (9 * 3 + 1)));

		dm = new DotMatrix();
		dmi = new DMImage[24];
		for (int i = 0; i < dmi.length; i++) {
			dmi[i] = new DMImage();
		}

		mode = DMMode.XYZ;

		this.addMouseListener(this);
		this.addMouseMotionListener(this);
		init();
	}

	public void setDotMatrix(DotMatrix dm) {
		this.dm = dm;
	}

	public DotMatrix getDotMatrix() {
		return this.dm;
	}

	private int getIndex(int row, int blockID, int blockC, int blockR) {
		int index = -1;

		switch (this.mode) {
		case YZX:
			switch (row) {
			case 0:
				index = blockID * 64 + blockC * 8 + blockR;
				break;
			case 1:
				index = (7 - blockC) * 64 + blockID * 8 + blockR;
				break;
			case 2:
				index = (7 - blockR) * 64 + blockC * 8 + blockID;
				break;
			}
			break;
		case ZXY:
			switch (row) {
			case 0:
				index = blockC * 64 + blockR * 8 + blockID;
				break;
			case 1:
				index = blockID * 64 + blockR * 8 + 7 - blockC;
				break;
			case 2:
				index = blockC * 64 + blockID * 8 + 7 - blockR;
				break;
			}
			break;
		case XYZ:
		default:
			switch (row) {
			case 0:
				index = blockR * 64 + blockID * 8 + blockC;
				break;
			case 1:
				index = blockR * 64 + (7 - blockC) * 8 + blockID;
				break;
			case 2:
				index = blockID * 64 + (7 - blockR) * 8 + blockC;
				break;
			}
			break;
		}

		return index;
	}

	private void init() {
		this.update();
	}

	public void update() {
		for (int i = 0; i < DotMatrix.DOT_LENGTH; i++) {
			int x, y, z;
			switch (mode) {

			case YZX:
				z = i % 8;
				x = i / 8 % 8;
				y = i / 64;
				break;
			case ZXY:
				y = i % 8;
				z = i / 8 % 8;
				x = i / 64;
				break;
			case XYZ:
			default:
				x = i % 8;
				y = i / 8 % 8;
				z = i / 64;
				break;
			}

			boolean val = dm.getDot(i);

			dmi[y].setDot(z * 8 + x, val);
			dmi[8 + x].setDot(z * 8 + 7 - y, val);
			dmi[16 + z].setDot((7 - y) * 8 + x, val);
		}

		for (DMImage image : dmi) {
			image.update();
		}
	}

	@Override
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D) g;
		g2d.setBackground(Color.lightGray);
		g2d.clearRect(0, 0, this.getWidth(), this.getHeight());

		for (int r = 0, y = DMImage.getBlockWidth(); r < 3; r++, y += DMImage
				.getBlockWidth() * 9) {
			for (int c = 0, x = DMImage.getBlockWidth(); c < 8; c++, x += DMImage
					.getBlockWidth() * 9) {
				int index = r * 8 + c;
				g2d.drawImage(dmi[index], x, y, null);
			}
		}
	}

	public void setMode(DMMode mode) {
		this.mode = mode;
	}

	@Override
	public void mouseClicked(MouseEvent e) {

	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (e.getButton() == MouseEvent.BUTTON1) {
			int blockID = getBlockID(e.getX());
			int blockC = getBlockColumn(e.getX());
			int blockR = getBlockRow(e.getY());

			if (blockID >= 8 || blockC < 0 || blockR < 0)
				return;

			int index = getIndex(getRow(e.getY()), blockID, blockC, blockR);

			if (e.isShiftDown()) {
				dm.setDot(index, true);
			} else if (e.isControlDown()) {
				dm.setDot(index, false);
			}else {
				dm.reverseDot(index);
			}

			update();
			repaint();
		} else if (e.getButton() == MouseEvent.BUTTON2) {
			int blockID = getBlockID(e.getX());

			if (blockID >= 8 || getBlockColumn(e.getX()) < 0 || getBlockRow(e.getY()) < 0)
				return;

			for (int x = 0 ; x < 8; x++) {
				for (int y = 0; y < 8; y++) {
					int index = getIndex(getRow(e.getY()), blockID, x, y);

					if (e.isShiftDown()) {
						dm.setDot(index, true);
					} else if (e.isControlDown()) {
						dm.setDot(index, false);
					}else {
						dm.reverseDot(index);
					}
				}
			}

			update();
			repaint();
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {

	}

	@Override
	public void mouseEntered(MouseEvent e) {

	}

	@Override
	public void mouseExited(MouseEvent e) {

	}

	@Override
	public void mouseDragged(MouseEvent e) {

	}

	@Override
	public void mouseMoved(MouseEvent e) {

	}

	private static int getBlockColumn(int x) {
		return (x / DMImage.getBlockWidth()) % 9 - 1;
	}

	private static int getBlockRow(int y) {
		return (y / DMImage.getBlockWidth()) % 9 - 1;
	}

	private static int getBlockID(int x) {
		return (x / DMImage.getBlockWidth()) / 9;
	}

	private static int getRow(int y) {
		return (y / DMImage.getBlockWidth()) / 9;
	}
}
