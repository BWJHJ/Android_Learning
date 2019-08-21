package com.dji.P4MissionsDemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

import client.solder.com.qrcodelibrary.QRCodeUtil;

public class QRcode {
    public static Mat get(Mat src) {

        //筛选方框轮廓大小
        int Max_size = 150;
        int Min_size = 50;
        //闭运算核大小
        int Element_size =10;
        //二值化阈值
        int bin_threshold = 100;
        //canny函数的两个参数
        int canny_min =200;
        int canny_max = 300;

        Mat img_src = src.clone();
        Imgproc.resize(img_src, img_src, new Size(4056.0, 3040.0));
        Mat img_img = img_src.clone();
        Imgproc.pyrDown(img_src, img_src);
        Imgproc.pyrDown(img_src, img_src);
        Mat img_bin = new Mat();
        Mat img_canny = new Mat();

        Imgproc.cvtColor(img_src, img_bin, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(img_bin, img_bin);
        Imgproc.threshold(img_bin, img_bin, bin_threshold, 255, Imgproc.THRESH_BINARY);
        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(Element_size, Element_size));
        Imgproc.morphologyEx(img_bin, img_canny, Imgproc.MORPH_CLOSE, element);
        Imgproc.Canny(img_canny, img_canny, canny_min, canny_max);

        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(img_canny, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
        int temp = contours.size();
        Point[] rect = new Point[4];
        for (int i = 0; i < temp; i++) {
            MatOfPoint2f newMtx = new MatOfPoint2f(contours.get(i).toArray());
            RotatedRect rotRect = Imgproc.minAreaRect(newMtx);
            double w = rotRect.size.width;
            double h = rotRect.size.height;

            if (w > Max_size || h > Max_size || w < Min_size || h < Min_size || Math.abs(w - h) > 10)
                continue;
            rotRect.points(rect);
            break;
        }
        //Point[] srcPoints=new Point[4];
        //Point[] dstPoints=new Point[4];

        Mat srcPoints = new Mat(4, 2, CvType.CV_32F);
        Mat dstPoints = new Mat(4, 2, CvType.CV_32F);

        for (int i = 0; i < 3; i++) {
            for (int j = i + 1; j < 4; j++) {
                if (rect[i].x > rect[j].x) {
                    double temp_num = rect[i].x;
                    rect[i].x = rect[j].x;
                    rect[j].x = temp_num;
                    temp_num = rect[i].y;
                    rect[i].y = rect[j].y;
                    rect[j].y = temp_num;
                }
            }
        }

        if (rect[0].y < rect[1].y) {
            srcPoints.put(0, 0, rect[0].x*4);
            srcPoints.put(0, 1, rect[0].y*4);
            srcPoints.put(1, 0, rect[1].x*4);
            srcPoints.put(1, 1, rect[1].y*4);
        } else {
            srcPoints.put(0, 0, rect[1].x*4);
            srcPoints.put(0, 1, rect[1].y*4);
            srcPoints.put(1, 0, rect[0].x*4);
            srcPoints.put(1, 1, rect[0].y*4);
        }
        if (rect[2].y > rect[3].y) {
            srcPoints.put(2, 0, rect[2].x*4);
            srcPoints.put(2, 1, rect[2].y*4);
            srcPoints.put(3, 0, rect[3].x*4);
            srcPoints.put(3, 1, rect[3].y*4);
        } else {
            srcPoints.put(2, 0, rect[3].x*4);
            srcPoints.put(2, 1, rect[3].y*4);
            srcPoints.put(3, 0, rect[2].x*4);
            srcPoints.put(3, 1, rect[2].y*4);
        }
        dstPoints.put(0, 0, 0);
        dstPoints.put(0, 1, 0);
        dstPoints.put(1, 0, 0);
        dstPoints.put(1, 1, 400);
        dstPoints.put(2, 0, 400);
        dstPoints.put(2, 1, 400);
        dstPoints.put(3, 0, 400);
        dstPoints.put(3, 1, 0);

        Mat dst = new Mat(400, 400, CvType.CV_8SC3);
        Mat transMat = Imgproc.getPerspectiveTransform(srcPoints, dstPoints);
        Imgproc.warpPerspective(img_img, dst, transMat, dst.size());
        Imgproc.cvtColor(dst, dst, Imgproc.COLOR_BGR2GRAY);
        Imgproc.equalizeHist(dst, dst);
        Imgproc.threshold(dst, dst, 160, 255, Imgproc.THRESH_BINARY);

        return dst;
    }

    public static int Find_Part_Position(Mat image) {

        Mat src = image.clone();
        Mat img=new Mat(500,500,CvType.CV_8UC3,new Scalar(255,255,255));
        Imgproc.cvtColor(img,img,Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(img,img,100,255,Imgproc.THRESH_BINARY);

        for (int i = 60; i < 440; i++) {
            for (int j = 60; j < 440; j++) {
                //img.get(i,j)[0]=src.get(i,j)[0];
                double[] temp_num = src.get(i-50,j-50);
                img.put(i,j,temp_num[0]);
            }
        }

        Mat element = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(10, 10));
        Imgproc.morphologyEx(img, img, Imgproc.MORPH_CLOSE, element);
        Imgcodecs.imwrite( Environment.getExternalStorageDirectory()+"/DJI_ScreenShot/tiaoshi.jpg",img);

        Mat edge = img.clone();
        Mat gray = img.clone();

        Imgproc.Canny(gray, edge, 5, 30);
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat(100, 4, CvType.CV_32FC1);
        Imgproc.findContours(edge, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_NONE);
        //画出所有边框：
        /*for(int i=0;i<contours.size();i++){
            Rect boundRect=new Rect();
            boundRect=Imgproc.boundingRect(contours.get(i));
            Imgproc.rectangle(src,new Point(boundRect.x,boundRect.y),new Point(boundRect.x+boundRect.width,boundRect.y+boundRect.height), new Scalar(0,255,0),2);
        }*/

        int ic = 0;
        int parentIdx = -1;
//        for (int i = 0; i < contours.size(); i++) {
//            Rect boundRect = new Rect();
//            boundRect = Imgproc.boundingRect(contours.get(i));
//

        //轮廓数量
        for(int t=0;t<hierarchy.cols();t++) {
            //遍历每一个轮廓
            double[] tempnumber = hierarchy.get(0, t);
            if (tempnumber[3] != -1) {
                parentIdx = t;
                ic++;
                tempnumber = hierarchy.get(0, parentIdx);
                while (tempnumber[3] != -1) {
                    ic++;
                    parentIdx = (int) tempnumber[3];
                    tempnumber = hierarchy.get(0, parentIdx);
                }
                if (ic != 2)
                    ic = 0;
            } else
                continue;



            Point tl = new Point();
            Point br = new Point();
            Rect boundRect = new Rect();
            boundRect = Imgproc.boundingRect(contours.get(parentIdx));

            if(boundRect.height  > 300 || boundRect.width > 300 || boundRect.height  < 200 || boundRect.width < 200 || Math.abs(boundRect.height  - boundRect.width) > 15)
                continue;

            Imgproc.rectangle(img,new Point(boundRect.x,boundRect.y),new Point(boundRect.x+boundRect.width,boundRect.y+boundRect.height), new Scalar(0,255,0),2);
            Imgcodecs.imwrite( Environment.getExternalStorageDirectory()+"/DJI_ScreenShot/mid.jpg",img);
            tl = boundRect.tl();
            br = boundRect.br();
            if ((tl.x + br.x) / 2 > 250) {
                //右上
                if ((tl.y + br.y) / 2 < 250)
                    return 1;
                    //右下
                else
                    return 2;
            } else {
                //左上
                if ((tl.y + br.y) / 2 < 250)
                    return 3;
                    //左下
                else
                    return 4;
            }
        }
        return -1;
    }

    public  static Mat QuChong(Mat image) {

        //K查重范围
        int K_Min = 20;
        int K_Max = 100;
        Mat src_image = image.clone();
        int m = 0;
        int n = 0;
        m = src_image.rows();
        n = src_image.cols();
        double ch_max = 0.5;
        int len_x = 20;
        int len_y = 20;
        Mat delete_row = new Mat();
        Mat delete_col = new Mat();
        for (int k = K_Min; k < K_Max; k++) {

            float chong = 0.0f;
            for (int i = m / 2; i < m / 2 + k; i++) {

                for (int j = 1; j < n; j++) {
                    double[] pixle_a = src_image.get(i, j);
                    double[] pixle_b = src_image.get(i - k-1 , j);
                    if ((int) pixle_a[0] == (int) pixle_b[0])
                        chong++;
                }
            }
            if (chong / k > ch_max) {
                ch_max = chong / k;
                len_x = k;
            }
        }
        for (int i = 0; i < m / 2 - len_x / 2; i++)
            delete_row.push_back(src_image.row(i));
        for (int i = m / 2 + len_x / 2 + 1; i < m; i++)
            delete_row.push_back(src_image.row(i));

        m = delete_row.rows();
        n = delete_row.cols();
        ch_max = 0.0f;
        for (int k = K_Min; k < K_Max; k++)//参数需调！！！！！！！！！！！！！
        {
            float chong = 0.0f;
            for (int i = n / 2; i < n / 2 + k; i++) {
                for (int j = 1; j < m; j++) {
                    double[] pixle_a = delete_row.get(j, i);
                    double[] pixle_b = delete_row.get(j, i - k - 1);

                    if ((int) pixle_a[0] == (int) pixle_b[0])//比对第(n/2,n/2+k)与(n/2-k-1,n/2-1)列
                        chong++;
                }
            }
            if (chong / k > ch_max) {
                ch_max = chong /k;
                len_y = k;
            }
        }

        delete_row = delete_row.t();
        for (int i = 0; i < n / 2 - len_y / 2; i++)
            delete_col.push_back(delete_row.row(i));
        for (int i = n / 2 + 1 + len_y / 2 + 1; i < n; i++)
            delete_col.push_back(delete_row.row(i));
        delete_col = delete_col.t();

        return delete_col;
    }


    public static String Extractionmain() {

        String Path1= Environment.getExternalStorageDirectory()+"/DJI_ScreenShot/1.jpg";
        String Path2= Environment.getExternalStorageDirectory()+"/DJI_ScreenShot/2.jpg";
        String Path3= Environment.getExternalStorageDirectory()+"/DJI_ScreenShot/3.jpg";
        String Path4= Environment.getExternalStorageDirectory()+"/DJI_ScreenShot/4.jpg";

        Mat img_a = Imgcodecs.imread(Path1);
        Mat img_b = Imgcodecs.imread(Path2);
        Mat img_c = Imgcodecs.imread(Path3);
        Mat img_d = Imgcodecs.imread(Path4);

        //Mat img_a= Imgcodecs.imread(Path1);
//        org.opencv.android.Utils.bitmapToMat(picture_a, img_a);

        Mat src_a = get(img_a);
        Imgcodecs.imwrite( Environment.getExternalStorageDirectory()+"/DJI_ScreenShot/a11.jpg",src_a);

        //view_1.setImageBitmap(pic_1);

        //Mat img_b= Imgcodecs.imread(Path2);
//        org.opencv.android.Utils.bitmapToMat(picture_b, img_b);
        Mat src_b = get(img_b);
        Imgcodecs.imwrite( Environment.getExternalStorageDirectory()+"/DJI_ScreenShot/b11.jpg",src_b);
//        Utils.matToBitmap(src_b, pic_2);
//        view_2.setImageBitmap(pic_2);

        //Mat img_c= Imgcodecs.imread(Path3);
//        org.opencv.android.Utils.bitmapToMat(picture_c, img_c);
        Mat src_c = get(img_c);
        Imgcodecs.imwrite( Environment.getExternalStorageDirectory()+"/DJI_ScreenShot/c11.jpg",src_c);
//        Utils.matToBitmap(src_c, pic_3);
//        view_3.setImageBitmap(pic_3);


        //Mat img_d= Imgcodecs.imread(Path4);
//        Utils.bitmapToMat(picture_d, img_d);
        Mat src_d = get(img_d);
        Imgcodecs.imwrite( Environment.getExternalStorageDirectory()+"/DJI_ScreenShot/d11.jpg",src_d);
//        Utils.matToBitmap(src_d, pic_4);
//        view_4.setImageBitmap(pic_4);
//
//
        //Toast.makeText(FuncTcpClient.context, "提取块儿成功", Toast.LENGTH_SHORT).show();

        int a=0;
        int b=0;
        int c=0;
        int d=0;
//
        a = Find_Part_Position(src_a);
        b = Find_Part_Position(src_b);
        c = Find_Part_Position(src_c);
        d = Find_Part_Position(src_d);

        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println(d);

////
////
//
//
//
        int miss_num = 0;
        for (int i = 1; i < 5; i++)
        {
            if (i != a && i != b && i != c && i != d)
                miss_num = i;
        }
        if (a < 0)
            a = miss_num;
        else if(b<0)
            b = miss_num;
        else if(c<0)
            c = miss_num;
        else
            d = miss_num;

//        if(a != b && a != c && a != d && b != c && b != d && c != d && a*b*c*d >0){
////            Toast.makeText(FuncTcpClient.context, "找位置成功", Toast.LENGTH_SHORT).show();
////        }


        System.out.println(a);
        System.out.println(b);
        System.out.println(c);
        System.out.println(d);
//
//            src_a=Imgcodecs.imread(Environment.getExternalStorageDirectory()+"/Pictures/Screenshots/a11.jpg",0);
//            Imgproc.cvtColor(src_a, src_a, Imgproc.COLOR_BGR2GRAY);
//        System.out.println(src_a);
//        Imgproc.threshold(src_a, src_a, 120, 255, Imgproc.THRESH_BINARY);
//        System.out.println(src_a);
//        System.out.println(src_b);
        Mat pin_a=new Mat();
        Mat pin_b=new Mat();
        Mat pin_c=new Mat();
        Mat pin_d=new Mat();

        if (a == 1)
            pin_a = src_a.clone();
        else if(a==2)
            pin_d = src_a.clone();
        else if(a==3)
            pin_b = src_a.clone();
        else
            pin_c = src_a.clone();

        if (b == 1)
            pin_a = src_b.clone();
        else if (b == 2)
            pin_d = src_b.clone();
        else if (b == 3)
            pin_b = src_b.clone();
        else
            pin_c = src_b.clone();

        if (c == 1)
            pin_a = src_c.clone();
        else if (c == 2)
            pin_d = src_c.clone();
        else if (c == 3)
            pin_b = src_c.clone();
        else
            pin_c = src_c.clone();

        if (d == 1)
            pin_a = src_d.clone();
        else if (d == 2)
            pin_d = src_d.clone();
        else if (d == 3)
            pin_b = src_d.clone();
        else
            pin_c = src_d.clone();
////
////        System.out.println(pin_a);
////        System.out.println(pin_b);
////        System.out.println(pin_c);
////        System.out.println(pin_d);
        Mat up=new Mat();
        Mat down=new Mat();
        Mat dst_qrimg=new Mat();

        List<Mat> new_mat1=new ArrayList<Mat>(2);
        new_mat1.add(pin_b);
        new_mat1.add(pin_a);
        Core.hconcat(new_mat1,up);

        List<Mat> new_mat2=new ArrayList<Mat>(2);
        new_mat2.add(pin_c);
        new_mat2.add(pin_d);
        Core.hconcat(new_mat2,down);

        List<Mat> new_mat3=new ArrayList<Mat>(2);
        new_mat3.add(up);
        new_mat3.add(down);
        Core.vconcat(new_mat3,dst_qrimg);
//
        Mat final_image=QuChong(dst_qrimg);
        int m=final_image.rows();
        int n=final_image.cols();

        Mat img=new Mat(100+m,100+n,CvType.CV_8UC3,new Scalar(255,255,255));
        Imgproc.cvtColor(img,img,Imgproc.COLOR_BGR2GRAY);
        Imgproc.threshold(img,img,100,255,Imgproc.THRESH_BINARY);


        for (int i = 60; i < m+40; i++) {
            for (int j = 60; j < n + 40; j++) {
                //img.get(i,j)[0]=src.get(i,j)[0];
                double[] temp_num = final_image.get(i - 50, j - 50);
                img.put(i, j, temp_num[0]);
            }
        }
        Imgcodecs.imwrite(Environment.getExternalStorageDirectory()+"/DJI_ScreenShot/B.jpg",img);
        String path=Environment.getExternalStorageDirectory()+"/DJI_ScreenShot/B.jpg";
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        String result = QRCodeUtil.decodeFromPhoto(bitmap);
//        System.out.println(result);
//        Bitmap bitmap = BitmapFactory.decodeFile(path);
//        view_1.setImageBitmap(bitmap);
        return result;


    }
}
