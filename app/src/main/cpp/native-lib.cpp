#include <jni.h>
#include <vector>
#include <android/log.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>

#define TAG "NativeLib"

using namespace std;
using namespace cv;

extern "C" JNIEXPORT void JNICALL
Java_com_elamed_opencvtest_MainActivity_adaptiveThresholdFromJNI(
        JNIEnv *env, jobject,
        jlong matAddr, jlong etalonMatAddr){
        clock_t begin = clock();
        Mat &mat = *(Mat *) matAddr;//create img and etalon Mat
        Mat &etalon = *(Mat *) etalonMatAddr;

        if( !mat.empty()){
            __android_log_print(ANDROID_LOG_ERROR,TAG,"have mat");
            if(!etalon.empty()){
                __android_log_print(ANDROID_LOG_ERROR,TAG,"have etalon");
                //convert images
                cvtColor(mat,mat,COLOR_RGBA2BGR);
                cvtColor(etalon,etalon,COLOR_RGBA2BGR);
                cvtColor(mat,mat,COLOR_BGR2HSV);
                cvtColor(etalon,etalon,COLOR_BGR2HSV);


                //calculate histogram
                vector<Mat> hls;
                vector<Mat> hlsEtalon;
                split(mat,hls);
                split(etalon,hlsEtalon);
                int bins = 256;
                float range[] = { 0, 256 }; //the upper boundary is exclusive
                const float* histRange = { range };

                Mat HLSmat[3];
                Mat HLSetalon[3];
                Mat hist[3];
                Mat xScarr, yScarr;
                Scharr(hls.at(1),xScarr,CV_8UC1,1,0);
                Scharr(hls.at(1),yScarr,CV_8UC1,0,1);
                mat=xScarr+yScarr;
                __android_log_print(ANDROID_LOG_INFO,TAG,"type image %d",mat.type());
                //for (int i=0;i<3;i++){
                //    calcHist(&hls[i],1,0,Mat(),HLSmat[i],1,&bins, &histRange);
                //    calcHist(&hlsEtalon[i],1,0,Mat(),HLSetalon[i],1,&bins,&histRange);
                    //normalize(HLSmat[i],HLSmat[i],0,1,cv::NORM_MINMAX,CV_8UC1);
                    //normalize(HLSetalon[i],HLSetalon[i],0,1,cv::NORM_MINMAX,CV_8UC1);
                //    hist[i]=HLSmat[i]-HLSetalon[i];
                //}
                //for (int i=0;i<hist[0].cols;i++){
                //    for (int j = 0; j <hist[0].rows ; j++) {
                //        __android_log_print(ANDROID_LOG_ERROR, TAG, "data col %d row %d %f",i,j,hist[1].at<float>(j, i));
                //    }
                // }
                //__android_log_print(ANDROID_LOG_INFO,TAG,"Hist col = %d  row= %d",HLSetalon[0].cols,HLSetalon[0].rows);

                double totalTime = double(clock()-begin)/CLOCKS_PER_SEC;
                __android_log_print(ANDROID_LOG_INFO, TAG,
                                    "Pavel this work adaptiveThreshold computation time = %f seconds\n",totalTime);

            }else{
                __android_log_print(ANDROID_LOG_ERROR,TAG,"Etalon is empty");
            }
        }else{
            __android_log_print(ANDROID_LOG_ERROR,TAG,"Image is empty");
        }



}

