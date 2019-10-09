import { Component, OnInit, NgZone, ViewChild, ElementRef } from '@angular/core';
import { Image } from "tns-core-modules/ui/image";
import { ImageAsset } from 'tns-core-modules/image-asset/image-asset';
import { CameraPlus, ICameraOptions } from '@nstudio/nativescript-camera-plus';
import {registerElement} from "nativescript-angular/element-registry";
import { ImageSource } from 'tns-core-modules/image-source/image-source';
import domtoimage from 'dom-to-image';
import { setInterval, clearInterval } from "tns-core-modules/timer";
const plugin = require("nativescript-screenshot");
registerElement('CameraPlus', () => <any>CameraPlus);
@Component({
  selector: 'ns-camera',
  templateUrl: './camera.component.html',
  styleUrls: ['./camera.component.css'],
  moduleId: module.id
})
export class CameraComponent implements OnInit {
    private cam: CameraPlus;
    public imageSource: ImageSource;
    options: ICameraOptions;
    picture: Image;

    @ViewChild('cameraView', {static:false}) dp: ElementRef;
    constructor(private zone: NgZone) {}
    getpreviewData(event) {

    }
    ngOnInit(): void {
        this.options.height = 300;
        this.options.confirm = false;
    }
    ngOnDestroy() {}

    public camLoaded(e: any): void {
      console.log('***** cam loaded *****');
      this.cam = e.object as CameraPlus;
      this.cam.autoFocus = false;
      const flashMode = this.cam.getFlashMode();


      // TEST THE ICONS SHOWING/HIDING
      // this.cameraPlus.showCaptureIcon = true;
      // this.cameraPlus.showFlashIcon = true;
      // this.cameraPlus.showGalleryIcon = false;
      // this.cameraPlus.showToggleIcon = false;
    }

    public imagesSelectedEvent(e: any): void {
      console.log('IMAGES SELECTED EVENT!!!');
      this.loadImage((e.data as ImageAsset[])[0]);
    }

    public photoCapturedEvent(e: any): void {
      console.log('PHOTO CAPTURED EVENT!!!');
      this.loadImage(e.data as ImageAsset);
      console.log(this.imageSource.height);
    }

    public toggleCameraEvent(e: any): void {
      console.log('camera toggled');
    }

    public recordDemoVideo(): void {
      try {
        console.log(`*** start recording ***`);
        this.cam.record();
      } catch (err) {
        console.log(err);
      }
    }

    public stopRecordingDemoVideo(): void {
      try {
        console.log(`*** stop recording ***`);
        this.cam.stop();
        console.log(`*** after this.cam.stop() ***`);
      } catch (err) {
        console.log(err);
      }
    }

    public toggleFlashOnCam(): void {
      this.cam.toggleFlash();
    }

    public toggleShowingFlashIcon(): void {
      console.log(`showFlashIcon = ${this.cam.showFlashIcon}`);
      this.cam.showFlashIcon = !this.cam.showFlashIcon;
    }

    public toggleTheCamera(): void {
      this.cam.toggleCamera();
    }

    public openCamPlusLibrary(): void {
      this.cam.chooseFromLibrary();
    }

    public takePicFromCam(): void {
      this.cam.takePicture({ saveToGallery: true });
    }

    private loadImage(imageAsset: ImageAsset): void {
      if (imageAsset) {
        this.imageSource = new ImageSource();

        this.imageSource.fromAsset(imageAsset).then(
          imgSrc => {
            if (imgSrc) {
              this.zone.run(() => {
                this.imageSource = imgSrc;
              });
            } else {
              this.imageSource = null;
              alert('Image source is bad.');
            }
          },
          err => {
            this.imageSource = null;
            console.log('Error getting image source: ');
            console.error(err);
            alert('Error getting image source from asset');
          }
        );
      } else {
        console.log('Image Asset was null');
        alert('Image Asset was null');
        this.imageSource = null;
      }
    }

}
