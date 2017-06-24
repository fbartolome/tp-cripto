package ar.edu.itba.criptog2.recover;

import ar.edu.itba.criptog2.Worker;
import ar.edu.itba.criptog2.util.BmpParser;
import ar.edu.itba.criptog2.util.LagrangeInterpolator;
import ar.edu.itba.criptog2.util.Polynomial;
import net.sourceforge.argparse4j.inf.Namespace;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Recoverer implements Worker {

	private List<BmpParser> pictures = new ArrayList<>();

	private int k;

	private byte[] secretPicture;


	private Recoverer() {
		this.k = 8;
		try {
			this.pictures.add(new BmpParser("img/Facundossd.bmp"));
			this.pictures.add(new BmpParser("img/Gustavossd.bmp"));
			this.pictures.add(new BmpParser("img/Jamesssd.bmp"));
			this.pictures.add(new BmpParser("img/Albertssd.bmp"));
			this.pictures.add(new BmpParser("img/Alfredssd.bmp"));
			this.pictures.add(new BmpParser("img/Audreyssd.bmp"));
			this.pictures.add(new BmpParser("img/Marilynssd.bmp"));
			this.pictures.add(new BmpParser("img/Evassd.bmp"));
			//TODO: set appropiate size
			this.secretPicture = new byte[pictures.get(0).getPictureSize()];
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Recoverer createFromNamespace(final Namespace ns) {
		final Recoverer recoverer = new Recoverer();
//		k = ns.getInt("k");
		// setup recoverer
		
		return recoverer;
	}
	
	@Override
	public void work() {
		List<Point> points;
		LagrangeInterpolator lagrangeInterpolator = new LagrangeInterpolator();
		int byteCount = 0;
		for(int j = 0; j < pictures.get(0).getPictureSize()/k; j++){
//		paso 1: agarro los primeros 8 bytes de cada foto y consigo un byte por cada una de esas fotos
			points = getPoints(j);

//		paso 2: encuentro el polinomio
			Polynomial polynomial = lagrangeInterpolator.interpolate(points,257);
			int[] coeffs = polynomial.getCoefficients();

//		paso 3: armo el pedacito de imagen del secreto
			for(int c : coeffs){
				secretPicture[byteCount++] = (byte)c;
			}

//		paso 4: repito el procedimiento

		}

//		paso 5: reordeno los bytes de la imagen secreto
		randomizeTable(pictures.get(0).getSeed());
	}

	private List<Point> getPoints(int j){
		List<Point> points = new ArrayList<>();
		for(BmpParser bmp : pictures){
			points.add(new Point(bmp.getShadowNumber(),getHiddenByte(bmp,j)));
		}
		return points;
	}

	private int getHiddenByte(BmpParser bmp, int j) {
		byte[] picData = bmp.getPictureData();
		String byteStr = "";
		for(int i = 0; i < 8; i++){
			byteStr += ((int)picData[8*j + i] & 1);
//			System.out.println(Integer.toBinaryString((picData[8*j + i]) & 255 | 256).substring(1));
		}
		return Integer.parseInt(byteStr, 2);
	}

	private void randomizeTable(final int seed){
		Random rnd = new Random(seed);
		for(int i=0; i < secretPicture.length; i++){
			secretPicture[i] = (byte) ((int)secretPicture[i] ^ rnd.nextInt(256));

		}
	}



}
