/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Util;

import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Eldrey
 */
public class ExibeImagem {

    public void exibirImagemTela(BufferedImage imagem, String texto) {
        JFrame frame = new JFrame();
        JLabel label = new JLabel(new ImageIcon(imagem));
        frame.getContentPane().add(label, BorderLayout.CENTER);
        frame.pack();
        frame.setTitle(texto);
        frame.setVisible(true);
        frame.setResizable(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }

    public BufferedImage convertMatToBuffer(Mat m) {
        Mat m2 = new Mat();
        byte[] b = null;
        int type = BufferedImage.TYPE_BYTE_GRAY;
        if (m.channels() > 1) {
            Imgproc.cvtColor(m, m2, Imgproc.COLOR_BGR2RGB);
            type = BufferedImage.TYPE_3BYTE_BGR;
        }
        int blen = m.channels() * m.cols() * m.rows();
        if (b == null || b.length != blen) {
            b = new byte[blen];
        }
        m2.get(0, 0, b);
        BufferedImage image = new BufferedImage(m.cols(), m.rows(), type);
        image.getRaster().setDataElements(0, 0, m.cols(), m.rows(), b);
        return image;
    }

    public void saveImg(BufferedImage imagem, String name) {

        File outputfile = new File(name);
        String tipo = name.split("\\.")[1];
        try {
            ImageIO.write(imagem, tipo, outputfile);
        } catch (IOException ex) {
            Logger.getLogger(ExibeImagem.class.getName()).log(Level.SEVERE, null, ex);

        }

    }
}
