/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package testdownloder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Observable;


public class Download extends Observable implements Runnable {
    //defining status by int 
    public static final int DOWNLOADING = 0;
    public static final int PAUSED = 1;
    public static final int STOPED = 2;
    public static final int COMPLETE = 3;
    public static final int ERROR = 4;
    
    private int status;
    private URL url;
    private String fileName;
    private double downloaded;
    private String saveFilePath;
    private double size=0.00;
    private int speed=0;

    
    public Download(URL url,String saveFilePath) {

        this.url = url;
        downloaded=0;
        status = DOWNLOADING;
        this.saveFilePath=saveFilePath;
        download();

    }
    
    
    public String getStatusString() {
        if(status==DOWNLOADING){
            return "Downloading";
        }
        else if(status==PAUSED){
            return "Paused";
        }
        else if(status==STOPED){
            return "Stop";
        }
        else if(status==COMPLETE){
           return "Completed";
        }
        else if(status==ERROR){
            return "Error";
        }
        return null;
    }

    public URL getUrl() {
        return url;
    }  
    
    public int getProgressSize() {
        double y=downloaded/size;
        int x=(int)(y*100);
      //  System.out.println("Progress "+x+"%");
        return x;
    }
    //speed in KB
    public int getSpeed(){
        return speed/1024;
    }
    //time in sec
    public int getRemainingTime(){
       return(int) ((size-downloaded)/speed);
    }
    
    /*public String getRemainingTime(){
        String s=null;
        int t=0;
        if(status==DOWNLOADING){
            t=(int) ((size-downloaded)/speed);
            s=Integer.toString(t);
        }
        int x=0;
        for(int i=0;i<3;i++){
            if(t>=60){
                t/=60;
                x++;
            }
        }
        if(x==0){
            s=s.concat("sec");
        }
        if(x==1){
            s=s.concat("min");
        }
        if(x==2){
            s=s.concat("hr");
        }
        return s;
    }*/
    
    public String getFileName() {
        return fileName;
    }
    
    //size returned in KB
    public int getSize() {
        return (int)size/1024;
    }
    
    public void stop() {
        status = STOPED;
        System.out.println("Stoped");
    }
    public void pause(){
        status = PAUSED;
        System.out.println("Paused");
        updateStatus();
        
    }
    public void resume(){
        status = DOWNLOADING;
        System.out.println("Resume");
        download();
    }
    //start download
    private void download() {
        Thread thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        
        
        BufferedInputStream in=null;
        FileOutputStream out=null;
        try{
            String txtUrl=url.getFile();
            fileName=txtUrl.substring(txtUrl.lastIndexOf('/') + 1);
            
            //setting the connection
            HttpURLConnection connection= (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoOutput(true);
            
           //get code from http response message
            int requestInfo=connection.getResponseCode();
            System.out.println("response code"+requestInfo);
            
            
            in= new BufferedInputStream(connection.getInputStream());
            
            
            
            
            System.out.println("Content length=" + connection.getContentLength());
            if(connection.getContentLength()<0){
                status=ERROR;
                updateStatus();
            }
            else{
                size=connection.getContentLength();
                updateStatus();
            }
            
            
            //file created 
            out=new FileOutputStream(saveFilePath + File.separator +fileName);
            
           
            byte[] buff=new byte[102400];
            int len=in.read(buff);
            
           
           
           //buffer is written in file until download complete
            while(status==DOWNLOADING && len!=-1){
                out.write(buff,0,len);
                speed=len;
                downloaded+=len;
                updateStatus();
                System.out.println("download="+downloaded);
                len=in.read(buff);
            }
            
            //Download is completed   
            if(status==DOWNLOADING){
                System.out.println("Download Completed");
                status=COMPLETE;
                updateStatus();
            }   
        }
        catch(Exception e){
            System.out.println("Can not download the file");
            status=ERROR;
            updateStatus();
        }
        finally{
            if(out != null){
                try {
                    out.flush();
                    out.close();
                } catch (Exception e) { }
            }
            if(in != null){
                try{
                    in.close();
                    System.out.println("in closed");
                }catch(Exception e){ }
            }
        }
        
    }

    private void updateStatus() {
        int x=countObservers();
        System.out.println("Status updated and number of Observers="+x);
        setChanged();
        notifyObservers();
    }
}
