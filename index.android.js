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

  statusPermission(callback) {
    samsungHealth.statusPermission((res) => {
      callback(res);
    });
  }

  stop() {
    samsungHealth.disconnect();
  }

  getDailyStepCountSamples(options, callback) {
    console.log("getDailyStepCounts");

    let startDate = options.startDate != undefined ? Date.parse(options.startDate) : (new Date()).setHours(0,0,0,0);
    let endDate = options.endDate != undefined ? Date.parse(options.endDate) : (new Date()).valueOf();
    let mergeData = options.mergeData != undefined ? options.mergeData : true;

    //console.log("startDate:" + startDate);
    //console.log("endDate:" + endDate);
    //console.log("startDate2:" + (new Date(startDate)).toLocaleString());
    //console.log("endDate2:" + (new Date(endDate)).toLocaleString());

    samsungHealth.readStepCount(startDate, endDate,
      (msg) => { callback(msg, false); },
      (res) => {
          if (res.length>0) {
              var resData = res.map(function(dev) {
                  var obj = {};
                  obj.source = dev.source.name;
                  obj.steps = this.buildDailySteps(dev.steps);
                  obj.sourceDetail = dev.source;
                  return obj;
                }, this);

              if (mergeData) resData = this.mergeResult(resData);

              callback(false, resData);
          } else {
              callback("There is no any steps data for this period", false);
          }
      }
    );
  }

  usubscribeListeners() {
    DeviceEventEmitter.removeAllListeners();
  }

  buildDailySteps(steps)
  {
      results = {}
      for(var step of steps) {
          var date = new Date(step.start_time);

          var day = ("0" + date.getDate()).slice(-2);
          var month = ("0" + (date.getMonth()+1)).slice(-2);
          var year = date.getFullYear();
          var dateFormatted = year + "-" + month + "-" + day;

          if (!(dateFormatted in results)) {
              results[dateFormatted] = 0;
          }

          results[dateFormatted] += step.count;
      }

      results2 = [];
      for(var index in results) {
          results2.push({date: index, value: results[index]});
      }
      return results2;
  }

  mergeResult(res)
  {
      results = {}
      for(var dev of res)
      {
          if (!(dev.sourceDetail.group in results)) {
              results[dev.sourceDetail.group] = {
                  source: dev.source,
                  sourceDetail: { group: dev.sourceDetail.group },
                  stepsDate: {}
              };
          }

          let group = results[dev.sourceDetail.group];

          for (var step of dev.steps) {
              if (!(step.date in group.stepsDate)) {
                  group.stepsDate[step.date] = 0;
              }

              group.stepsDate[step.date] += step.value;
          }
      }

      results2 = [];
      for(var index in results) {
          let group = results[index];
          var steps = [];
          for(var date in group.stepsDate) {
              steps.push({
                date: date,
                value: group.stepsDate[date]
              });
          }
          group.steps = steps.sort((a,b) => a.date < b.date ? -1 : 1);
          delete group.stepsDate;

          results2.push(group);
      }

      return results2;
  }

}

export default new RNSamsungHealth();

/* vim :set ts=4 sw=4 sts=4 et : */
