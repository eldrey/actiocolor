/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ActioColor;

import Util.Matematica;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

/**
 *
 * @author Eldrey
 */
class Processamento {

    private static final float anguloDeuterotopia = (float) ((Math.PI / 180) * -8.11);
    private static final int L = 0;
    private static final int A = 1;
    private static final int B = 2;
    private static int largura;
    private static int altura;
    private static final Matematica mate = new Matematica();

    private static float[][] matrizPerdaContraste;
    private static int contOperacao;
    private static int[] posicaoGaussiana;
    private static float anguloRotacao;

    private static final float[] pixelOriginal = new float[3];
    private static final float[] pixelVizinho = new float[3];
    private static final float[] pixelOriginalDet = new float[3];
    private static final float[] pixelVizinhoDet = new float[3];

    private static float distOriginalVizinho;
    private static float distOriginalVizinhoDet;
    private static float direcaoPerdaContraste;
    private static float[][] matrizFinal;
    private static float[] autovalor;
    private static float[][] autovetor;
    private static float senoAngRotacao;
    private static float cossenoAngRotacao;
    private static float[] oldPixel;
    private static float[] newPixel;

    public Mat processamento(Mat labImagem_32FC3) {

        Mat labImagem_32FC3_Simulado = projetorDeuteranotopia(labImagem_32FC3);

        altura = (int) labImagem_32FC3.size().height;
        largura = (int) labImagem_32FC3.size().width;

        matrizPerdaContraste = new float[altura * largura * 2][2];
        contOperacao = 0;

        for (int ROW = 0; ROW < altura; ROW++) {
            for (int COL = 0; COL < largura; COL++) {

                labImagem_32FC3.get(ROW, COL, pixelOriginal);
                posicaoGaussiana = mate.calcDistriGaussi(COL, ROW, largura, altura);

                labImagem_32FC3.get(posicaoGaussiana[1], posicaoGaussiana[0], pixelVizinho);

                labImagem_32FC3_Simulado.get(ROW, COL, pixelOriginalDet);
                labImagem_32FC3_Simulado.get(posicaoGaussiana[1], posicaoGaussiana[0], pixelVizinhoDet);

                distOriginalVizinho = (float) (Math.sqrt((pixelOriginal[A] - pixelVizinho[A]) * (pixelOriginal[A] - pixelVizinho[A])
                        + (pixelOriginal[B] - pixelVizinho[B]) * (pixelOriginal[B] - pixelVizinho[B])));

                distOriginalVizinhoDet = (float) (Math.sqrt((pixelOriginalDet[A] - pixelVizinhoDet[A]) * (pixelOriginalDet[A] - pixelVizinhoDet[A])
                        + (pixelOriginalDet[B] - pixelVizinhoDet[B]) * (pixelOriginalDet[B] - pixelVizinhoDet[B])));

                if (distOriginalVizinho == 0) {
                    direcaoPerdaContraste = 0;
                } else {
                    direcaoPerdaContraste = ((distOriginalVizinho - distOriginalVizinhoDet) / distOriginalVizinho);
                }

                matrizPerdaContraste[contOperacao][0] = (pixelOriginal[A] - pixelVizinho[A]) * direcaoPerdaContraste;
                matrizPerdaContraste[contOperacao][1] = (pixelOriginal[B] - pixelVizinho[B]) * direcaoPerdaContraste;
                contOperacao++;

            }
        }

        matrizFinal = mate.multiplicaMatriz(matrizPerdaContraste, mate.matrizTransposta(matrizPerdaContraste));
        autovalor = mate.autovalor(matrizFinal);
        autovetor = mate.autovetor(matrizFinal, autovalor);

        if (autovetor[0][0] > autovetor[1][0]) {
            anguloRotacao = (float) Math.atan2(autovetor[0][0], autovetor[0][1]);
        } else {
            anguloRotacao = (float) Math.atan2(autovetor[1][0], autovetor[1][1]);
        }

        return rotacionaImagem(labImagem_32FC3, anguloRotacao);
    }

    private static Mat rotacionaImagem(Mat imagemLab, float angRotacao) {

        Mat imagemFinal = new Mat(imagemLab.size(), CvType.CV_32FC3);

        senoAngRotacao = (float) mate.getSeno(angRotacao);
        cossenoAngRotacao = (float) mate.getCosseno(angRotacao);

        oldPixel = new float[3];
        newPixel = new float[3];
        for (int ROW = 0; ROW < altura; ROW++) {
            for (int COL = 0; COL < largura; COL++) {

                imagemLab.get(ROW, COL, oldPixel);

                newPixel[L] = oldPixel[L];
                newPixel[A] = oldPixel[B] * senoAngRotacao + oldPixel[A] * cossenoAngRotacao;
                newPixel[B] = oldPixel[B] * cossenoAngRotacao - oldPixel[A] * senoAngRotacao;

                imagemFinal.put(ROW, COL, newPixel);
            }

        }
        return imagemFinal;
    }

    public static Mat projetorDeuteranotopia(Mat imagem) {
        int imgH = (int) imagem.size().height;
        int imgW = (int) imagem.size().width;
        Mat imagDeut = new Mat(imagem.size(), CvType.CV_32FC3);
        double[] pixel = new double[3];

        float angCor;
        float raioCor;
        float escala;

        for (int i = 0; i < imgH; i++) {
            for (int j = 0; j < imgW; j++) {

                pixel = imagem.get(i, j);
                angCor = (float) Math.atan2(pixel[A], pixel[B]);
                raioCor = (float) Math.sqrt(pixel[A] * pixel[A] + pixel[B] * pixel[B]);
                escala = (float) (raioCor * mate.getCosseno(Math.abs(anguloDeuterotopia - angCor)));
//                escala = (float) (raioCor * Math.cos(Math.abs(anguloDeuterotopia - angCor)));

                pixel[A] = escala * mate.getSeno(anguloDeuterotopia);
//                pixel[A] = escala * Math.sin(anguloDeuterotopia);
                pixel[B] = escala * mate.getCosseno(anguloDeuterotopia);
//                pixel[B] = escala * Math.cos(anguloDeuterotopia);

                imagDeut.put(i, j, pixel);
            }
        }

        return imagDeut;
    }

}
