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
    samsungHealth.readStepCount(
      (msg) => { callback(msg, false); },
      (res) => { callback(false, res); }
    );
  }

  usubscribeListeners() {
    DeviceEventEmitter.removeAllListeners();
  }

}

export default new RNSamsungHealth();

/* vim :set ts=4 sw=4 sts=4 et : */
