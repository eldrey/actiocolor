/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ActioColor;

import Util.ExibeImagem;
import java.io.IOException;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

/**
 *
 * @author Eldrey
 */
public class ActioColor {

    /**
     * @param args the command line arguments
     */
    private static boolean exibeImagem;

    private static final Processamento proc = new Processamento();
    private static final ExibeImagem exibe = new ExibeImagem();

    private static int divisor;
    private static int alturaOrignal;
    private static int larguraOriginal;
    private static double newH;
    private static double newW;
    private static final int L = 0, A = 1, B = 2;

    private static final float[] pixelTamOrigial = new float[3];
    private static final double[] pixelFinal = new double[3];
    private static double pesoFloorX;
    private static double pesoFloorY;
    private static double pesoCeilX;
    private static double pesoCeilY;
    private static double ceilX;
    private static double ceilY;
    private static double floorX;
    private static double floorY;
    private static double interPixelRow;
    private static double interPixelCol;
    private static String imagemPath;

    private static Mat imagemOriginal_8U;
    private static Mat imagemRGB_32FC3;
    private static Mat imagemLAB_32FC3;
    private static Mat imagem;
    private static Mat labImagem;
    private static Mat deutImagem;
    private static Mat imagemResult;
    private static Mat imagemLAB_32FC3_Reduzida;

    /**
     * @param args the command line arguments
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws IOException {

        //configurações de entrada
        exibeImagem = args[1].equals("T");
        imagemPath = args[0];
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        //double milisegundosStart = System.currentTimeMillis();

        //1° passo - ler a imagem imagemOriginal_8U
        imagemOriginal_8U = Highgui.imread(imagemPath);
        alturaOrignal = (int) imagemOriginal_8U.size().height;
        larguraOriginal = (int) imagemOriginal_8U.size().width;
        int numPixels = (alturaOrignal * larguraOriginal);
        if ((numPixels <= 786432)) {
            divisor = 4;
        } else if ((numPixels > 786432) && (numPixels <= 5038848)) {
            divisor = 6;
        } else if ((numPixels > 5038848) && (numPixels <= 9291264)) {
            divisor = 10;
        } else if ((numPixels > 9291264) && (numPixels <= 13543680)) {
            divisor = 12;
        } else if (numPixels > 13543680) {
            divisor = 1000;
        }
        if (exibeImagem) {
            exibe.exibirImagemTela(exibe.convertMatToBuffer(imagemOriginal_8U), "Original");
        }

        //2° passo - converter imagemOriginal_8U para imagemRGB_32FC3
        imagemRGB_32FC3 = new Mat(imagemOriginal_8U.size(), CvType.CV_32FC3);
        imagemOriginal_8U.convertTo(imagemRGB_32FC3, CvType.CV_32F, 1.0 / 255.0);

        //3° passo - converter imagemRGB_32FC3 para imagemLAB_32FC3
        imagemLAB_32FC3 = new Mat(imagemRGB_32FC3.size(), CvType.CV_32FC3);
        Imgproc.cvtColor(imagemRGB_32FC3, imagemLAB_32FC3, Imgproc.COLOR_BGR2Lab);

        if (exibeImagem) {
            imagem = new Mat(imagemOriginal_8U.size(), CvType.CV_32FC3);
            imagemOriginal_8U.convertTo(imagem, CvType.CV_32F, 1.0 / 255.0);

            labImagem = new Mat(imagem.size(), CvType.CV_32FC3);
            Imgproc.cvtColor(imagem, labImagem, Imgproc.COLOR_BGR2Lab);

            deutImagem = new Mat(imagem.size(), CvType.CV_8UC3);
            Imgproc.cvtColor(Processamento.projetorDeuteranotopia(labImagem), imagem, Imgproc.COLOR_Lab2BGR);
            imagem.convertTo(deutImagem, CvType.CV_8U, 255);
            exibe.exibirImagemTela(exibe.convertMatToBuffer(deutImagem), "Simulação");

        }

        imagemOriginal_8U = null;

        //4° passo - reduzir a imagemLAB_32FC3 para a imagemLAB_32FC3_Reduzida
        newH = imagemLAB_32FC3.size().height / divisor;
        newW = imagemLAB_32FC3.size().width / divisor;
        imagemLAB_32FC3_Reduzida = new Mat(new Size(Math.ceil(newW), Math.ceil(newH)), CvType.CV_32FC3);
        Imgproc.resize(imagemLAB_32FC3, imagemLAB_32FC3_Reduzida, new Size(newW, newH));

        //5° passo - processar a imagemLAB_32FC3_Reduzida e reaproveitar o buffer imagemLAB_32FC3_Reduzida
        imagemLAB_32FC3_Reduzida = proc.processamento(imagemLAB_32FC3_Reduzida);

        //imprime imagem reduzida
        if (exibeImagem) {

            imagemResult = new Mat(imagemRGB_32FC3.size(), CvType.CV_8U);

            Imgproc.cvtColor(imagemLAB_32FC3_Reduzida, imagemRGB_32FC3, Imgproc.COLOR_Lab2BGR);
            imagemRGB_32FC3.convertTo(imagemResult, CvType.CV_8U, 255.0);
            exibe.exibirImagemTela(exibe.convertMatToBuffer(imagemResult), "Imagem reduzida");
        }

        //6° passo - interpolar a imagemLAB_32FC3_Reduzida com a imagemLAB_32FC3
        for (int row = 0; row < alturaOrignal; row++) {
            for (int col = 0; col < larguraOriginal; col++) {
                imagemLAB_32FC3.get(row, col, pixelTamOrigial);

                interPixelCol = col / divisor;
                interPixelRow = row / divisor;
                if (interPixelCol == (larguraOriginal / divisor)) {
                    interPixelCol--;
                }
                if (interPixelRow == (alturaOrignal / divisor)) {
                    interPixelRow--;
                }
                floorX = Math.floor(interPixelCol);
                floorY = Math.floor(interPixelRow);
                ceilX = Math.ceil(interPixelCol);
                ceilY = Math.ceil(interPixelRow);

                if (interPixelCol == floorX || interPixelCol >= (larguraOriginal / divisor) - 1 || interPixelRow >= (alturaOrignal / divisor) - 1) {
                    pesoFloorX = 1;
                    pesoCeilX = 0;
                } else {
                    pesoFloorX = ceilX - (col / divisor);
                    pesoCeilX = (col / divisor) - floorX;
                }
                if (interPixelRow == floorY || interPixelRow >= (alturaOrignal / divisor) - 1 || interPixelCol >= (larguraOriginal / divisor) - 1) {
                    pesoFloorY = 1;
                    pesoCeilY = 0;
                } else {
                    pesoFloorY = ceilY - (row / divisor);
                    pesoCeilY = (row / divisor) - floorY;
                }
                pixelFinal[L] = pixelTamOrigial[L];
                if (pesoFloorY != 0 && pesoFloorX != 0) {
                    pixelFinal[A] = imagemLAB_32FC3_Reduzida.get((int) floorY, (int) floorX)[A] * pesoFloorY * pesoFloorX;
                    pixelFinal[B] = imagemLAB_32FC3_Reduzida.get((int) floorY, (int) floorX)[B] * pesoFloorY * pesoFloorX;
                }
                if (pesoCeilY != 0 && pesoFloorX != 0) {
                    pixelFinal[A] = pixelFinal[A] + imagemLAB_32FC3_Reduzida.get((int) ceilY, (int) floorX)[A] * pesoCeilY * pesoFloorX;
                    pixelFinal[B] = pixelFinal[B] + imagemLAB_32FC3_Reduzida.get((int) ceilY, (int) floorX)[B] * pesoCeilY * pesoFloorX;
                }
                if (pesoFloorY != 0 && pesoCeilX != 0) {
                    pixelFinal[A] = pixelFinal[A] + imagemLAB_32FC3_Reduzida.get((int) floorY, (int) ceilX)[A] * pesoFloorY * pesoCeilX;
                    pixelFinal[B] = pixelFinal[B] + imagemLAB_32FC3_Reduzida.get((int) floorY, (int) ceilX)[B] * pesoFloorY * pesoCeilX;
                }
                if (pesoCeilY != 0 && pesoCeilX != 0) {
                    pixelFinal[A] = pixelFinal[A] + imagemLAB_32FC3_Reduzida.get((int) ceilY, (int) ceilX)[A] * pesoCeilY * pesoCeilX;
                    pixelFinal[B] = pixelFinal[B] + imagemLAB_32FC3_Reduzida.get((int) ceilY, (int) ceilX)[B] * pesoCeilY * pesoCeilX;
                }

                imagemLAB_32FC3.put(row, col, pixelFinal);
            }
        }
        imagemLAB_32FC3_Reduzida = null;

        //7° passo - converter imagemLAB_32FC3 para imagemRGB_32FC3
        Imgproc.cvtColor(imagemLAB_32FC3, imagemRGB_32FC3, Imgproc.COLOR_Lab2BGR);

        //8° passo - converter imagemRGB_32FC3 para imagemRGB_8U
        imagemResult = new Mat(imagemRGB_32FC3.size(), CvType.CV_8U);
        imagemRGB_32FC3.convertTo(imagemResult, CvType.CV_8U, 255.0);

        byte[] return_buff = new byte[(int) (imagemResult.total() * imagemResult.channels())];
        imagemResult.get(0, 0, return_buff);
        if (exibeImagem) {
            //9° passo - exibir imagemRGB_8U
            Imgproc.cvtColor(imagemLAB_32FC3, imagemRGB_32FC3, Imgproc.COLOR_Lab2BGR);
            imagemRGB_32FC3.convertTo(imagemResult, CvType.CV_8U, 255.0);
            exibe.exibirImagemTela(exibe.convertMatToBuffer(imagemResult), "Resultado");
        }
        Imgproc.cvtColor(Processamento.projetorDeuteranotopia(imagemLAB_32FC3), imagemRGB_32FC3, Imgproc.COLOR_Lab2BGR);
        imagemRGB_32FC3.convertTo(imagemResult, CvType.CV_8U, 255.0);
        exibe.saveImg(exibe.convertMatToBuffer(imagemResult), imagemPath);

        if (exibeImagem) {
            //9° passo - exibir imagemRGB_8U
            Imgproc.cvtColor(Processamento.projetorDeuteranotopia(imagemLAB_32FC3), imagemRGB_32FC3, Imgproc.COLOR_Lab2BGR);
            imagemRGB_32FC3.convertTo(imagemResult, CvType.CV_8U, 255.0);
            exibe.exibirImagemTela(exibe.convertMatToBuffer(imagemResult), "Resultado simulado");
        }

        imagemResult = null;
        imagemLAB_32FC3 = null;
    }

}
