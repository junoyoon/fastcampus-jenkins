pipeline {
    // 어떠한 에이전트에서도 실행 가능함을 표현
    agent any

    environment {
        // jenkins 가 관리하는 도구의 위치는 이와 같이 환경 변수로 저장 가능
        SONAR_SCANNER_HOME = tool name: 'sonar-scanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
    }

    parameters {
        booleanParam(defaultValue: isDeploymentNecessary(), description: '배포 포함 여부', name: 'DEPLOY_ENABLED')
    }

    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '5', numToKeepStr: '5')
        githubProjectProperty(displayName: '', projectUrlStr: 'https://github.com/fastcampus-jenkins/fastcampus-jenkins')
        // 디폴트 checkout skip 설정 제거
    }

      triggers {
            issueCommentTrigger('.*(test this|build this|deploy this).*')
      }
      
  
    // stages > stage > steps 순으로 구성
    stages {
        stage('Build') {
            steps {
                // withGradle 을 하면, Gradle 로그를 해석
                dir("projects/spring-app") {
                    withGradle {
                        sh "./gradlew build"
                    }
                }
            }
        }

        stage('SonarScanner') {
            when {
                  expression {
                      return isSonarQubeNecessary()
                  }
              }
            steps {
                // sonarqube 환경하에서, 실행
                withSonarQubeEnv("sonarqube-server") {
                    sh """
                    ${env.SONAR_SCANNER_HOME}/bin/sonar-scanner \
                        -Dsonar.host.url=http://sonarqube:9000 \
                        -Dsonar.projectKey=sample \
                        -Dsonar.projectBaseDir=${WORKSPACE}/projects/spring-app
                  """
                }

                // quality gate 통과시 까지 대기
                timeout(time: 1, unit: 'MINUTES') {

                    // Parameter indicates whether to set pipeline to UNSTABLE if Quality Gate fails
                    // true = set pipeline to UNSTABLE, false = don't
                    waitForQualityGate abortPipeline: true
                }
            }
        }

    }

    post {
        always {
            scanForIssues tool: ktLint(pattern: '**/ktlint/**/*.xml')
            junit '**/test-results/**/*.xml'
            jacoco sourcePattern: '**/src/main/kotlin'
            script {
                if (isNotificationNecessary()) {
                    mineRepository()
                    emailext attachLog: true, body: email_content(), subject: email_subject(), to: 'junoyoon@gmail.com'
                    slackSend(channel: "#jenkins", message: "${custom_msg(currentBuild.currentResult)}")
                }
            }
        }

        success {
            script {
                  if (params.DEPLOY_ENABLED == true) {
                     archiveArtifacts artifacts: 'projects/spring-app/build/libs/*-SNAPSHOT.jar', followSymlinks: false
                     build(
                             job: 'pipeline-deploy',
                             parameters: [booleanParam(name: 'ARE_YOU_SURE', value: "true")],
                             wait: false,
                             propagate: false
                      )
                  }
                  if (isPr()) {
                      echo "pipeline-deploy 실행"
                      if (env.CHANGE_ID) {
                        pullRequest.comment('This PR invoked pipeline-deploy..')
                      }
                  }
            }
        }
    }
}




// pipeline 바깥쪽 영역은 groovy 사용 가능
def email_content() {
    return '''이 이메일은 중요한 것이여!!

${DEFAULT_CONTENT}

'''
}

def email_subject() {
    return '빌드통지!! - ${DEFAULT_SUBJECT}'
}

def custom_msg(status) {
    return " $status: Job [${env.JOB_NAME}] Logs path: ${env.BUILD_URL}/consoleText"
}



def isSonarQubeNecessary() {
    return isMainOrDevelop()
}

def isDeploymentNecessary() {
  return isMainOrDevelop() || (env.GITHUB_COMMENT ?: "").contains("deploy this")
}

def isNotificationNecessary() {
    return !isPr()
}

def isMainOrDevelop() {
    return (env.BRANCH_NAME == "develop" || env.BRANCH_NAME == "main")
}

def isPr() {
    return env.BRANCH_NAME.startsWith("PR-")
}
