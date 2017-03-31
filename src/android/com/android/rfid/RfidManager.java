package com.android.rfid;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

/**
 *
 * @Title: RfidManager.java
 * @Package  com.android.rfid
 * @Description: 用单例模式设计
 * @author Jimmy
 * @date 2014-2-28
 *
 */
public class RfidManager {

	//PSAM模块串口
	private PSAM psam = null ;
	//输入输出流
	private InputStream in = null ;
	private OutputStream out = null ;
	private static RfidManager manager = null ;
	//构造器私有化
	private RfidManager(int port, int baudRate) throws IOException{
		 powerOnMCU() ;
		this.psam = new PSAM(port, baudRate);
		if(this.psam == null){
			throw new IOException();
		}
		this.in = psam.getInputStream() ;
		this.out = psam.getOutputStream() ;
		try {
			Thread.sleep(200) ;
			byte[] rep = new byte[128] ;
			in.read(rep) ;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	//获取实例
	public static RfidManager getRfidMananger(int port, int baudRate) throws IOException{
		if(manager == null){
			manager = new RfidManager(port, baudRate);
		}
		return manager;
	}
	//给模块上电
	public void powerOnMCU(){
		if(psam != null){
			psam.PowerOn_HFPsam();
		}
	}
	//关闭模块电源
	public void powerOffMCU(){
		if(psam != null){
			psam.PowerOff_HFPsam();
		}
	}

	//获取版本号
	public byte[] getVersion(){
		byte[] version = null ;
		byte[] recv = null;
		byte[] cmd = psam.getversion();
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			version = psam.resolveDataFromDevice(recv);
			if(version != null){
				Log.i("getVersion", Tools.Bytes2HexString(version, version.length));
			}
		}
		return version ;
	}
	/**************************M1卡********************************/
	//寻卡
	public byte[] inventoryM1(){
		byte[] uid = null;
		byte[] recv = null ;
		byte[] cmd = psam.rf_card();
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			uid = psam.resolveDataFromDevice(recv);
			if(uid != null){
				Log.i("getVersion", Tools.Bytes2HexString(uid, uid.length));
			}
		}
		return uid;
	}

	/**
	 *
	 *@Method Description:
	 *@param passwordType 密码类型，0为密码A，1为密码B
	 *@param sector 扇区，传入的扇区应该为  扇区号x4
	 *@param password 密码
	 *@return
	 *@Autor Jimmy
	 *@Date 2014-4-21
	 */
	public boolean authM1(int passwordType, int sector, byte[] password){
		byte[] recv = null;
		byte[] cmd = null;
		boolean flag = false;
		byte[] info = new byte[8];
		info[0] = (byte) passwordType;
		info[1] = (byte) sector;
		System.arraycopy(password, 0, info, 2, 6);
		cmd = psam.rf_authentication_cmd(info);
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		recv = this.read();
		if(recv != null){
			if(psam.rf_check_data(recv) == 0){
				flag = true;
			}
		}
		return flag;
	}

	/**
	 *
	 *@Method Description: 14443A M1卡读块数据
	 *@param block
	 *@return
	 *@Autor Jimmy
	 *@Date 2014-4-21
	 */
	public byte[] readM1(int block){
		byte[] data = null;
		byte[] recv = null;
		byte[] cmd = null;
		byte[] blocks = new byte[1];
		blocks[0] = (byte) block;
		cmd = psam.rf_read_cmd(blocks);
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			data = psam.resolveDataFromDevice(recv);
		}
		return data;
	}

	/**
	 *
	 *@Method Description: M1卡写块数据
	 *@param block  块号

	 *@return
	 *@Autor Jimmy
	 *@Date 2014-4-21
	 */
	public boolean writeM1(int block, byte[] data){
		boolean flag = false;
		byte[] recv = null;
		byte[] cmd = null;
		byte[] info = new byte[17];
		info[0] = (byte)block;
		System.arraycopy(data, 0, info, 1, 16);
		cmd = psam.rf_write_cmd(info);
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			if(psam.rf_check_data(recv) == 0){
				flag = true;
			}
		}
		return flag;
	}


	/************************非接触CPU卡*****************************/
	//激活卡片
	public byte[] activeICcard(){
		byte[] uid = null;
		byte[] recv = null ;
		byte[] data = null ;
		byte[] cmd = psam.ucpu_open() ;
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			data = psam.resolveDataFromDevice(recv);
			if(data != null && data.length > 6){
				Log.i("getVersion", Tools.Bytes2HexString(data, data.length));
				int uidLen = data[1];
				if(uidLen >= data.length){
					return null;
				}
				uid = new byte[uidLen];
				System.arraycopy(data, 2, uid, 0, uidLen);
			}
		}
		return uid;
	}

	//关闭卡片
	public boolean closeICcard(){
		boolean closeFlag = false;
		byte[] recv = null ;
		byte[] data = null ;
		byte[] cmd = psam.ucpu_close() ;
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			int aa = psam.rf_check_data(recv);
			if(aa == 0){
				closeFlag = true;
			}

		}

		return closeFlag;
	}

	//APDU操作
	public byte[] icCardAPDU(byte[] apdu){
		byte[] recvData = null;
		byte[] recv = null ;
		byte[] data = null ;
		byte[] cmd = psam.ucpu_send_cmd(apdu) ;
		Log.i("CPU APDU", Tools.Bytes2HexString(cmd, cmd.length));
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			data = psam.resolveDataFromDevice(recv);
			if(data != null){
				Log.i("CPU APDU", Tools.Bytes2HexString(data, data.length));
			}

		}
		return data;
	}


	/*************************PSAM**********************************/

	//SAM卡上电复位
	public boolean samReset(int samCard){
		boolean resetFlag = false;
		byte[] recv = null;
		byte[] data = null;
		byte[] sam = Tools.HexString2Bytes("0"+samCard);
		byte[] cmd = psam.sam_reset(sam);
		Log.i("SAM RESET CMD", Tools.Bytes2HexString(cmd, cmd.length));
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			data = psam.resolveDataFromDevice(recv);
			if(data != null){
				Log.i("SAM RESET RETURN", Tools.Bytes2HexString(data, data.length));
				resetFlag = true;
			}
		}
		return resetFlag;
	}

	//SAM卡ADPU
	public byte[] samAPDU(byte[] apdu){
		byte[] recv = null;
		byte[] data = null;
		byte[] cmd = psam.sam_send_cmd(apdu);
		Log.i("SAM APDU", Tools.Bytes2HexString(cmd, cmd.length));
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			data = psam.resolveDataFromDevice(recv);
			if(data != null){
				Log.i("SAM APDU", Tools.Bytes2HexString(data, data.length));
			}
		}
		return data;
	}

	//SAM卡下电
	public void samShutDown(int sam){
		byte[] recv = null;
		byte[] data = null;
		byte[] samBytes = Tools.HexString2Bytes("0" + sam);
		byte[] cmd = psam.sam_shut_dowm(samBytes);
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			data = psam.resolveDataFromDevice(recv);
			if(data != null){
				Log.i("SAM shut down", Tools.Bytes2HexString(data, data.length));
			}
		}

	}

	/*****************************15693卡**********************************************/
	/**
	 *
	 *@Method Description:15693寻卡
	 *@return
	 *@Autor Jimmy
	 *@Date 2014-4-21
	 */
	public byte[] inventory15693(){
		byte[] uid = null;
		byte[] recv = null;
		byte[] cmd = null;
		cmd = psam.ISO15693_Inventory();
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			uid = psam.resolveDataFromDevice(recv);
		}
		return uid;
	}
	/**
	 *
	 *@Method Description:15693选卡指令
	 *@param uid 15693 uid
	 *@return
	 *@Autor Jimmy
	 *@Date 2014-4-21
	 */
	public boolean select15693(byte[] uid){
		boolean flag = false;
		byte[] recv = null;
		byte[] cmd = null;
		cmd = psam.ISO15693_Select(uid);
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			if(psam.rf_check_data(recv) == 0){
				flag = true;
			}
		}
		return flag ;
	}

	/**
	 *
	 *@Method Description:读15693块数据
	 *@param address 起始地址
	 *@param length 读数据长度
	 *@return
	 *@Autor Jimmy
	 *@Date 2014-4-21
	 */
	public byte[] read15693(int address, int length){
		byte[] data = null;
		byte[] recv = null;
		byte[] cmd = null;
		byte[] addr = {(byte)address};
		byte[] len = {(byte)length};
		cmd = psam.ISO15693_Read(addr, len);
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			data = psam.resolveDataFromDevice(recv);
		}
		return data;
	}

	/**
	 *
	 *@Method Description: 15693写块数据
	 *@param address 起始地址
	 *@param length  数据长度
	 *@param data  数据
	 *@return
	 *@Autor Jimmy
	 *@Date 2014-4-21
	 */
	public boolean write15693(int address, int length, byte[] data){
		boolean flag = false;
		byte[] recv = null;
		byte[] add = {(byte)address};
		length = length/2;
		byte[] len = {(byte) length};
		byte[] cmd = null;
		cmd = psam.ISO15693_Write(add, len, data);
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			if(psam.rf_check_data(recv) == 0){
				flag = true;
			}
		}
		return flag;
	}

	/**************读写寄存器*********************/
	/**
	 * 读寄存器
	 * @param addr  寄存器地址
	 * @return 返回一个字节的值
	 */
	public byte[] readReg(byte addr){
		byte[] recv = null;
		byte[] cmd = psam.readReg(addr);
		byte[] data = null;
		Log.e("readReg", Tools.Bytes2HexString(cmd, cmd.length));
		try {
			write(cmd);
		} catch (IOException e) {
			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
		recv = this.read();
		if(recv != null){
			Log.e("readReg", Tools.Bytes2HexString(recv, recv.length));
			data = psam.resolveDataFromDevice(recv);
		}
		return data;
	}

	/**
	 * 写寄存器
	 * @param addr  地址
	 * @param value  要写入的值
	 * @return  若为true写入成功，false写入失败
	 */
	public boolean writeReg(byte addr , byte[] value){
		byte[] recv = null;
		byte[] cmd = psam.writeReg(addr, value);
		if(cmd != null){
			Log.e("writeReg", Tools.Bytes2HexString(cmd, cmd.length));
			try {
				write(cmd);
			} catch (IOException e) {
				// TODO Auto-generated catch block
			}
		}
		recv = this.read();
		if(recv != null){
			Log.e("writeReg", Tools.Bytes2HexString(recv, recv.length));
			psam.resolveDataFromDevice(recv);
		}
		return false ;
	}

	/*******************************************/
	//关闭串口、输入输出流
	public void close(int port){
		powerOffMCU() ;
		if(psam == null){
			return;
		}
		try {
			this.in.close();
			this.out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		psam.close(port);
		//回到初始状态
		manager = null;

	}
	/**
	 *
	 *@Method Description:发送指令
	 *@param cmd 指令
	 * @throws IOException
	 *@Autor Jimmy
	 *@Date 2014-2-28
	 */
	private void write(byte[] cmd) throws IOException{
		this.out.write(cmd);
	}

	/**
	 *
	 *@Method Description:接收读写器返回数据包
	 *@return byte[] 响应数据
	 *@Autor Jimmy
	 *@Date 2014-2-28
	 */
	private byte[] read(){
		byte[] recv = null;
		int count = 0;
		int index = 0;
		try {
		while(count < 3){
			count = this.in.available();
			//延时500ms，超过500ms无数据返回，则作超时处理
			if(index > 50){
				return null;
			}
			index++;
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		  }
		Thread.sleep(20);
		count = this.in.available();
		recv = new byte[count] ;
		this.in.read(recv);
		if(recv != null){
			Log.i("read recv", Tools.Bytes2HexString(recv, recv.length));
		}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}




		return recv;
	}



}
