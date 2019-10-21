import { Component, OnInit, NgZone, ViewChild, ElementRef } from '@angular/core';
import { registerElement } from "nativescript-angular/element-registry";
import { AR, ARMaterial,ARPlaneTappedEventData } from "nativescript-ar";
import { Color } from 'tns-core-modules/color/color';
registerElement("AR", () => require("nativescript-ar").AR);


@Component({
  selector: 'ns-camera',
  templateUrl: './camera.component.html',
  styleUrls: ['./camera.component.css'],
  moduleId: module.id
})
export class CameraComponent {
    foundimage() {
      console.log('FOUND IMAGE');
      
    }
    constructor() {
      console.log("AR supported? " + AR.isSupported());
    }
    planeTapped(args: ARPlaneTappedEventData): void {
      console.log(`Plane tapped at ${args.position.x} y ${args.position.y} z ${args.position.z}`);
      const ar: AR = args.object;
      // interact with the 'ar' object here if you like
    }
  }
