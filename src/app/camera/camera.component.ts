import { Component, OnInit, NgZone, ViewChild, ElementRef } from '@angular/core';
import { registerElement } from "nativescript-angular/element-registry";
import { AR, ARMaterial,ARPlaneTappedEventData, ARTrackingImageDetectedEventData, ARLoadedEventData, ARCommonNode } from "nativescript-ar";
import { Color } from 'tns-core-modules/color/color';
registerElement("AR", () => require("nativescript-ar").AR);


@Component({
  selector: 'ns-camera',
  templateUrl: './camera.component.html',
  styleUrls: ['./camera.component.css'],
  moduleId: module.id
})
export class CameraComponent {
    ar: AR;
    box: Promise<ARCommonNode>;
    arLoaded(args: ARLoadedEventData) {
        console.log('loaded');

        this.ar = args.object;
    }
    foundimage() {

        this.box.then(ar => {
          console.log(ar.getWorldPosition());
        });

    }
    createBoxes() {
        this.ar.addBox({position: {
            x:1,
            y: 1,
            z: 1
        }, dimensions: {
          x: 0.15,
          y: 0.15,
          z: 0.15
        }, materials: [new Color("blue")],
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
    getCurrentPos() {
            console.log(this.ar);

    }
    constructor() {
      console.log("AR supported? " + AR.isSupported());
    }

    imageDetected(args: ARTrackingImageDetectedEventData ) {
      console.log(args.position, args.imageName);
       this.box = args.imageTrackingActions.addBox({position: {
          x: args.position.x,
          y: args.position.y,
          z: args.position.z
      }, dimensions: {
        x: 0.15,
        y: 0.15,
        z: 0.15
      }, materials: [new Color("blue")],
      draggingEnabled: false, })
    }
  }
