var jenkinsUrl = "http://localhost:8080/"
/*
 FIXME: 다음 API 를 적절히 수정 필요
 필요한 정보는 다음과 같음

 job리스트
 - 빌드리스트
   - 각빌드별 시간/결과/url
*/
const apiUrl = "/api/json"
//const apiUrl = "/api/json?pretty=true&tree=jobs[name,builds[number,result,timestamp,url],jobs[name,builds[number,result,timestamp,url]]]"

const { createApp, ref } = Vue
  createApp({
    setup() {
      const days = 28
      const jobs = ref(new Map())
      const dates = ref(new Array(days))

      axios.get(apiUrl)
        .then(function(response) {
            console.log(response.data)
            let flatted = response.data.jobs.flatMap(function(e) {

                    if (e.jobs === undefined) {
                        return [e]
                    }
                    else {
                        // FIXME: 아래 로직 변경 필요
                        return []
                        /*
                        return e.jobs.map(function(sub) {
                            sub.name = `${e.name}/${sub.name}`
                            return sub
                        })*/
                    }
                })
            const result = flatted.reduce((map, obj) => {
                map.set(obj.name, new Array(days))
                return map;
            }, new Map());

            let today = new Date().setHours(23, 59, 59, 999)
            flatted.forEach((e) => {
                e.builds.forEach((build) => {
                    var dayDiff = Math.floor(((today - build.timestamp) / (1000* 60 * 60 * 24)))
                    if (dayDiff <= days -1) {
                        buildStatus = result.get(e.name)[days - 1 - dayDiff]
                        let count = 1;
                        if (buildStatus !== undefined) {
                            count = buildStatus.count + 1
                        }

                        result.get(e.name)[days - 1 - dayDiff] = {
                            count: count,
                            result: build.result,
                            url: build.url
                        }
                    }
                })
            })

            /*
             다음과 같은 형태로 출력
             {
                "job명1" : [ { count:0, result: "SUCCESS", url: "http://joburl"}, { ...  },...],
                "job명2" : [ { ... }, { ... }, { ... }, ... , {   }]
             }
            */
            jobs.value = result

            /* 아래는 테이블 최상단의 날자 리스트를 출력하기 위한 로직임. 살펴볼 필요 없음 */
            const datesStr = [];
            for (let i = days-1; i >= 0; i--) {
              const date = new Date(today - i * 864e5);
              const month = date.getMonth() + 1;
              const day = date.getDate();
              datesStr.push(`${month}/${day}`);
            }

            dates.value = datesStr;

        })
      return {
        jobs,
        dates
      }
    },
    methods: {
        openBuild(build) {
            if (build === undefined) {
                return
            }
            // FIXME: 아래 로직 변경 필요
            let url = build.url
            //let url = jenkinsUrl + build.url.split('/').splice(3).join("/")
            open(url, "build")
        }
    }

  }).mount('#app')