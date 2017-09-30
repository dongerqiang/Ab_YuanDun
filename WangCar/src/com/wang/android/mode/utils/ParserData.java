package com.wang.android.mode.utils;

import com.wang.android.MyApplication;

public class ParserData {
	
	public int parserMileage(byte data[]){
		long huoerCount = data[0]&0xff;
		huoerCount <<=8;
		huoerCount |= data[1]&0xff;
		huoerCount <<=8;
		huoerCount |= data[2]&0xff;
		huoerCount <<=8;
		huoerCount |= data[3]&0xff;
		
		int km = (int) (MyApplication.app.deviceNotes.getWheel()* huoerCount*2) ;
		MyApplication.logBug("maileage huoerCount = "+huoerCount);
		MyApplication.logBug("maileage km = "+km);
		return km;
	}
	
	public int parserSpeed(byte data[]){
		float speedKm = 0;
		
		int speedHallCount = data[0] & 0xff;//0
		speedHallCount <<=8;
		speedHallCount |= data[1]&0xff;//1
		
		speedHallCount = speedHallCount *2* 3600;
		
		int yadiSpeedHallCount = data[2] & 0xff;
		yadiSpeedHallCount <<= 8;
		yadiSpeedHallCount |= data[3];
		yadiSpeedHallCount = yadiSpeedHallCount * 2 * 3600;
		
		
		MyApplication.logBug("speedHallCount  = "+speedHallCount);
		MyApplication.logBug("yadiSpeedHallCount = "+yadiSpeedHallCount);
		
		 float circumference = MyApplication.app.deviceNotes.getWheel();
		 
		if(yadiSpeedHallCount == 0){
			speedKm = 2.87f*1.2f*(speedHallCount*circumference/1000f)/(6f*MyApplication.app.deviceNotes.optMotorJds(false, 1));
		}else{
			speedKm = 2.87f*1.2f*(yadiSpeedHallCount*circumference/1000f)/6f/MyApplication.app.deviceNotes.optMotorJds(false, 1);
		}
		
		MyApplication.logBug("speedKm = "+speedKm);
		return (int) speedKm;
	}
	
	
	public float parserElectricQuantity(byte[] data){
		float percent = 0;
		int realBattery = data[2] &0xff;
		if(realBattery/100f<=0 ||realBattery/100f>1){
		
		int battery = data[0] & 0xff;
		battery <<= 8;
		battery |= data[1] & 0xff;
		battery = battery /10+1;
		int type = MyApplication.app.deviceNotes.opeMotorVol(false, 1);
		MyApplication.logBug("battery == "+battery+"\ntype =="+type);
		    
		       switch(type) {
	           case 12:
	               if (battery>=8&&battery<=20) {
	                   percent = (battery-8.0f)/(20.0f-8.0f);
	               }
	               break;
		        case 36:
		               if (battery>=32&&battery<=43) {
		                   percent = (battery-32.0f)/(43.0f-32.0f);
		               }
		               break;
		        case 48:
		               if (battery>=42&&battery<=59) {
		                   percent = (battery-42.0f)/(59.0f-42.0f);
		               }
		               break;
		        case 60:
		               /*if (battery>=50 &&battery<=61) {
		                   percent = (battery-50f)/(61-52.0f);
		               }*/
		        		if(battery<=50){
		        			 percent =0f;
		        		}else if(battery>50 &&battery<=61){
		        			percent = (battery-50.0f)/(61.0f-50.0f);
		        		}else{
		        			percent =1;
		        		}
		        		break;
		        case 64:
		               if (battery>=56&&battery<=78.8) {
		                   percent = (battery-56.0f)/(78.8f-56.0f);
		               }
		               break;
		        case 72:
		               if (battery>=64&&battery<=79) {
		                   percent = (battery-64.0f)/(79.0f-64.0f);
		               }
		               break;
		        case 80:
		               if (battery>=72&&battery<=89) {
		                   percent = (battery-72.0f)/(89.0f-72.0f);
		               }
		               break;
		        default:  
		        		percent=0;
		        		break;
		    }
		   	
		}else if(realBattery/100f<=1&&realBattery/100f>0){
			percent=realBattery/100f;
		}
       MyApplication.logBug("percent == "+percent);
       return percent;
	}
	
	public String parserASR(byte asr){
		StringBuffer sb = new StringBuffer();
		
		if((asr & 0x10) != 0){
			sb.append("霍尔故障\n");
		}
		
		if((asr & 0x08) != 0){
			sb.append("转把故障\n");
		}
		
		if((asr & 0x04) != 0){
			sb.append("控制器故障\n");
		}
		
		if((asr & 0x02) != 0){
			sb.append("欠压故障\n");
		}

		if((asr & 0x01) != 0){
			sb.append("刹车故障\n");
		}
		
		return sb.toString();
	}
	
	
}
