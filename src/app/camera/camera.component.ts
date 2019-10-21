import { Component, OnInit, NgZone, ViewChild, ElementRef } from '@angular/core';
import { registerElement } from "nativescript-angular/element-registry";
import { AR, ARMaterial,ARPlaneTappedEventData, ARTrackingImageDetectedEventData, ARLoadedEventData, ARCommonNode } from "nativescript-ar";
import { Color } from 'tns-core-modules/color/color';
import { Toasty } from 'nativescript-toasty';
registerElement("AR", () => require("nativescript-ar").AR);


@Component({
  selector: 'ns-camera',
  templateUrl: './camera.component.html',
  styleUrls: ['./camera.component.css'],
  moduleId: module.id
})
export class CameraComponent {
    ar: any;
    message: Toasty;
    box: Promise<ARCommonNode>;
    arLoaded(args: ARLoadedEventData) {
        console.log('loaded');

        this.ar = args.object;
    }
    getObjectPos() {
        this.box.then(ar => {
          console.log(ar.getWorldPosition());
        });
    }
    createBoxes() {
        new Toasty({text: 'Boxes created'}).show();
        this.ar.addBox({position: {
            x:1,
            y: 1,
            z: 1
        }, dimensions: {
          x: 0.15,
          y: 0.15,
          z: 0.15
        }, materials: [new Color("white")],
        draggingEnabled: false, });
        this.ar.addBox({position: {
            x:0,
            y: 0,
            z: 0
        }, dimensions: {
          x: 0.15,
          y: 0.15,
          z: 0.15
        }, materials: [new Color("blue")],
        draggingEnabled: false, });
    }
    getDevicePos() {
        console.log(this.ar.getCameraPosition());
    }
    constructor() {
      console.log("AR supported? " + AR.isSupported());
    }
    imageDetected(args: ARTrackingImageDetectedEventData ) {
      console.log(args.position, args.imageName);
      new Toasty({text: args.position + args.imageName + ' found.'}).show();
       this.box = args.imageTrackingActions.addBox({position: {
          x: args.position.x,
          y: args.position.y,
          z: args.position.z
      }, dimensions: {
        x: 0.15,
        y: 0.15,
        z: 0.15
      }, materials: [new Color("red")],
      draggingEnabled: false, })
    }
  }
