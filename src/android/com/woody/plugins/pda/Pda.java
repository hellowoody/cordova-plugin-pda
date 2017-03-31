package com.woody.plugins.pda;

import android.util.Log;

import com.android.rfid.PSAM;
import com.android.rfid.RfidManager;
import com.android.rfid.Tools;
import com.handheld.UHF.UhfManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by woody on 2017/3/29.
 * 用于安卓 pda 型号c5000
 * 在安装CPU+超高频功能模块 识别相应的卡
 */

public class Pda extends CordovaPlugin
{
  private static final String LOG_TAG = "WOODY_PLUGINS_PDA";
  // Cordova 3.x.x has a copy of this plugin bundled with it (SplashScreenInternal.java).
  // Enable functionality only if running on 4.x.x.
  /**
   * Displays the splash drawable.
   */
  private PSAM mpsam ;
  /**
   * Remember last device orientation to detect orientation changes.
   */
  private boolean runFlag = true;
  private boolean startFlag = false;
  private UhfManager uhfManager ; // UHF manager,UHF Operating handle
  private ArrayList<Map<String, Object>> listMap;
  private int power = 0 ;//rate of work
  private int area = 0;



  @Override
  public void onResume(boolean multitasking) {

  }
  @Override
  public void onPause(boolean multitasking) {
    if (uhfManager != null) {
      uhfManager.close();
    }
  }

  @Override
  public void onDestroy() {
    // If we set this to true onDestroy, we lose track when we go from page to page!
    //firstShow = true;
    if (uhfManager != null) {
      uhfManager.close();
    }
  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    JSONObject r = new JSONObject();
    RfidManager manager = null;
    try {
      manager = RfidManager.getRfidMananger(14, 115200);
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    manager.powerOnMCU();
    mpsam = new PSAM();//实例化mpsam,用于调用协议封装命令
    //识别S50卡 卡号
    if (action.equals("m1")) {
      byte []cmd = null; //用于存放指令
      cmd = mpsam.rf_card(); //获取命令
      String cmd_find_card = Tools.Bytes2HexString(cmd, cmd.length);
      Log.e("", cmd_find_card);
      // play sound
      Util.play(1, 0);
      byte[] uid14443A = manager.inventoryM1();
      String cardNo = "";
      if(uid14443A != null){
        System.out.println();
        cardNo = Tools.Bytes2HexString(uid14443A, uid14443A.length);
        Log.e("uid14443A===>>",cardNo);
      }
      r.put("cardNo",cardNo);
      callbackContext.success(r.toString());
    }
    //识别复旦cpu卡
    else if(action.equals("cpu")){
      byte[] uid = manager.activeICcard();
      if(uid != null){
        Log.i("uid " , Tools.Bytes2HexString(uid, uid.length));
      }
      r.put("activeCPU",Tools.Bytes2HexString(uid, uid.length));
      // play sound
      Util.play(1, 0);
      //选择主目录3F00
      String strAPDU = "00A40000023F00";
      byte[] apdu = Tools.HexString2Bytes(strAPDU);
      byte[] cpuData = manager.icCardAPDU(apdu);
      String res = "";
      String res2 = "";
      String res3 = "";
      if(cpuData != null){
        Log.e("CPU APDU DATA===>>", Tools.Bytes2HexString(cpuData, cpuData.length));
        res = Tools.Bytes2HexString(cpuData, cpuData.length);
        r.put("cpuContent1",res);
        //最后4位如果是9000 代表成功
        if("9000".equals(res.substring(res.length()-4,res.length())))
        {
          //选择0001文件
          String strAPDU2 = "00A40000020001";
          byte[] apdu2 = Tools.HexString2Bytes(strAPDU2);
          byte[] cpuData2 = manager.icCardAPDU(apdu2);
          if(cpuData2 != null)
          {
            Log.e("CPU APDU DATA22222===>>", Tools.Bytes2HexString(cpuData2, cpuData2.length));
            res2 = Tools.Bytes2HexString(cpuData2, cpuData2.length);
            r.put("cpuContent2",res2);
            //最后4位如果是9000 代表成功
            if("9000".equals(res2.substring(res2.length()-4,res2.length())))
            {
              //读当前文件的前99字节
              String strAPDU3 = "00B0000099";
              byte[] apdu3 = Tools.HexString2Bytes(strAPDU3);
              byte[] cpuData3 = manager.icCardAPDU(apdu3);
              if(cpuData3 != null){
                Log.e("CPU APDU DATA3333===>>", Tools.Bytes2HexString(cpuData3, cpuData3.length));
                res3 = Tools.Bytes2HexString(cpuData3, cpuData3.length);
                r.put("cpuContent3",res3);
              }
            }
          }
        }
      }
      callbackContext.success(r.toString());
    }else if(action.equals("uhf"))
    {
      runFlag = true;
      startFlag = true;
      Thread thread = new Pda.InventoryThread(callbackContext);
      thread.start();
    }
    else if(action.equals("stopuhf"))
    {
      runFlag = false;
      startFlag = false;
      callbackContext.success(r.toString());
    }
    else
    {
      return false;
    }
    return true;
  }


  /**
   * @woody 2017 03 29
   * Inventory EPC Thread
   * 起一个新线程去不停扫描ufh卡
   */
  class InventoryThread extends Thread {

    private CallbackContext callbackContext;
    private JSONObject jsonObject = new JSONObject();
    private List<byte[]> epcList;
    public InventoryThread(CallbackContext callbackContext)
    {
      this.callbackContext = callbackContext;
    }
    @Override
    public void run() {
      super.run();
      int count = 0;
      uhfManager = UhfManager.getInstance();
      power = 30; //单位 dbm
      area = UhfManager.WorkArea_USA;
      uhfManager.setOutputPower(power);
      uhfManager.setWorkArea(area);
      List<String> listRes = new ArrayList<String>();
      while (runFlag) {
        if (startFlag) {
          epcList = uhfManager.inventoryRealTime(); // inventory real time
          if (epcList != null && !epcList.isEmpty()) {
            for (byte[] epc : epcList) {
              String epcStr = Tools.Bytes2HexString(epc,
                epc.length);
              addEpcList(listRes,epcStr);
            }
          }
          epcList = null;
          try {
            Thread.sleep(40);
          } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
          }
        }
      }
      for(String epc : listRes)
      {
        try {
          jsonObject.put("card"+count,epc);
          count++;
        } catch (JSONException e) {
          e.printStackTrace();
        }
      }
      callbackContext.success(jsonObject.toString());
    }
  }
  private void addEpcList(List<String> listRes,String epcStr)
  {
    if (listRes.isEmpty())
    {
      // play sound
      Util.play(1, 0);
      listRes.add(epcStr);
    }else{
      for (int i = 0; i < listRes.size(); i++) {
        if (epcStr.equals(listRes.get(i)))
        {
          break;
        }
        else if(i == (listRes.size() - 1))
        {
          // play sound
          Util.play(1, 0);
          listRes.add(epcStr);
        }
      }
    }
  }
}
