const { createApp, ref } = Vue
  createApp({
    setup() {
      const days = 28
      const jobs = ref(new Map())
      const dates = ref(new Array(days))

      axios.get("http://localhost:8080//api/json?pretty=true&depth=1&tree=jobs[name,builds[number,result,timestamp,url],jobs[name,builds[number,result,timestamp,url]]]")
        .then(function(response) {
            let flatted = response.data.jobs.flatMap(function(e) {
                    if (e.jobs === undefined) {
                        return [e]
                    } else {
                        return e.jobs.map(function(sub) {
                            sub.name = `${e.name}/${sub.name}`
                            return sub
                        })
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

            const datesStr = [];
            for (let i = days-1; i >= 0; i--) {
              const date = new Date(today - i * 864e5);
              const month = date.getMonth() + 1;
              const day = date.getDate();
              datesStr.push(`${month}/${day}`);
            }

            dates.value = datesStr;
            jobs.value = result
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
            open(build.url, "build")
        }
    }

  }).mount('#app')