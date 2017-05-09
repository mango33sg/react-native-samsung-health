import { 
  NativeModules,
  DeviceEventEmitter
} from 'react-native';

const samsungHealth = NativeModules.RNSamsungHealth;

class RNSamsungHealth {
  constructor() {
  }

  authorize(callback) {
    samsungHealth.connect(
      (msg) => { callback(msg, false); },
      (res) => { callback(false, res); }
    );
  }

  getDailyStepCountSamples(options, callback) {
    console.log("getDailyStepCounts");

    console.log("startDate:" + options.startDate);
    let startDate = options.startDate != undefined ? Date.parse(options.startDate) : (new Date()).setHours(0,0,0,0);
    let endDate = options.endDate != undefined ? Date.parse(options.endDate) : (new Date()).valueOf();
    console.log("startDate:" + startDate);
    console.log("endDate:" + endDate);
    samsungHealth.readStepCount(startDate, endDate,
      (msg) => { callback(msg, false); },
      (res) => {
          if (res.length>0) {
              callback(false, res);
          } else {
              callback("There is no any steps data for this period", false);
          }
      }
    );
  }

  usubscribeListeners() {
    DeviceEventEmitter.removeAllListeners();
  }

}

export default new RNSamsungHealth();

/* vim :set ts=4 sw=4 sts=4 et : */
