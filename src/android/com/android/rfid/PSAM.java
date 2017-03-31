package com.android.rfid;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;


/**
 * 
 * 此类为jni层方法的调用,请不要随意修改native方法
 * */
public class PSAM {

	
	static{
		Log.e("", "load libs");
		System.loadLibrary("devapi");
		System.loadLibrary("PSAM");
	}
	
	private FileDescriptor mFd;
	private FileInputStream mFileInputStream;
	private FileOutputStream mFileOutputStream;
	/*不要对mFd重命名，因为mFd在关闭的时候要用到*/
	public PSAM(){}  //无参构造方法
	
	public PSAM(int port,int baudrate) throws IOException{
		mFd = open(port, baudrate);   //文件描述符用于创建InputStream和OutputStream
		if (mFd == null) {
			Log.e("", "native open returns null");
			throw new IOException();
		}
		mFileInputStream = new FileInputStream(mFd);
		mFileOutputStream = new FileOutputStream(mFd);
	}
	
	
	
	//Getters and Setters
	public InputStream getInputStream() {
		return mFileInputStream;
	}

	public OutputStream getOutputStream() {
		return mFileOutputStream;
	}
	
	
	
	//JNI方法
	private native static FileDescriptor open(int port, int baudrate);   //打开串口
	public native void close(int port);									 //关闭串口
	public native int PowerOn_HFPsam();									 //开启设备电源
	public native int PowerOff_HFPsam();	 							 //关闭设备电源
	public native byte[] getversion();									 //获取设备版本命令
	public native byte[] resolveDataFromDevice(byte[] resourceData);	 //解析从设备传来的数据
	
	/*====================M1卡==================================*/
	public native byte[] rf_card();										  //寻卡指令
	public native byte[] rf_authentication_cmd(byte [] sectorAndpassword);//认证指令  
	public native int rf_check_data(byte [] auth);						  //校验返回（检验data区为空的返回值）
	public native byte[] rf_read_cmd(byte [] part);					      //读块值
//	public native byte[] resolve_read_data(byte[] data);				  //解析读数据（分两次传输的数据）
	public native byte[] rf_write_cmd(byte [] value);					  //写块值
	
	/*====================PSAM卡=================================*/
	public native byte[] sam_reset(byte []card);						  //上电复位
	public native byte[] sam_send_cmd(byte []senddata);					  //发送指令（APDU）
	public native byte[] sam_shut_dowm(byte []card);					  //下电
	
	/*=======================非接触CPU卡==========================*/
	public native byte[] ucpu_open();									  //激活卡片
	public native byte[] ucpu_close();									  //关闭卡片
	public native byte[] ucpu_send_cmd(byte []data);					  //发送指令（APDU）
	
	/*=====================15693=================================*/
	public native byte[] ISO15693_Inventory();							  //15693寻卡指令
	public native byte[] ISO15693_Select(byte []uid);					  //选卡
	public native byte[] ISO15693_Read(byte []area, byte []value);  	  //读数据
	public native byte[] ISO15693_Write(byte []area, byte []value, byte []data);		  //写数据
	
	/*======================射频开关函数=============================*/
	public native byte[] open_rf();										 //开启射频
	public native byte[] close_rf();									 //关闭射频
	
	/*======================读写寄存器=============================*/
	/**读寄存器
	 * @param addr  
	 * @return
	 */
	public native byte[] readReg(byte addr);										 //读寄存器
	/**
	 * 写寄存器
	 * @param addr
	 * @param value
	 * @return
	 */
	public native byte[] writeReg(byte addr , byte[] value);						 //写寄存器
	
}
