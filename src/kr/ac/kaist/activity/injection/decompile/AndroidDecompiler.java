/*******************************************************************************
* Copyright (c) 2016 IBM Corporation and KAIST.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License v1.0
* which accompanies this distribution, and is available at
* http://www.eclipse.org/legal/epl-v10.html
*
* Contributors:
* KAIST - initial API and implementation
*******************************************************************************/
package kr.ac.kaist.activity.injection.decompile;

import brut.androlib.ApkDecoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

public class AndroidDecompiler {
	public static String decompile(String apk){
		ApkDecoder decoder = new ApkDecoder();

		if(!apk.endsWith(".apk"))
			throw new InternalError("only support apk file : " + apk);
		File apkFile = new File(apk);
		if(!apkFile.exists())
			throw new InternalError("the file does not exist : " + apk);

		try{
			String path = apkFile.getCanonicalPath();
			String toPath = path.substring(0, path.length()-4);
			decoder.setForceDelete(true);
			decoder.setOutDir(new File(toPath));
			decoder.setApkFile(apkFile);
			decoder.decode();

			permission(toPath);
			return toPath;
		}catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}
	
	private static void permission(String path){
		String[] cmd = {"chmod", "755", path};
		ProcessBuilder pb = new ProcessBuilder();
		pb.command(cmd);
		
		Process p = null;
		try {
			p = pb.start();
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			BufferedReader bre = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			String r = null;
			
			while((r = br.readLine()) != null){
				System.out.println(r);
			}
			
			while((r = bre.readLine()) != null){
				System.err.println(r);
			}
			
			int res = p.waitFor();
			if(res != 0){
				throw new InternalError("failed to decompile: " + path);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
