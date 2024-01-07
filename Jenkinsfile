pipeline {
    // 어떠한 에이전트에서도 실행 가능함을 표현
    agent any

    environment {
        // jenkins 가 관리하는 도구의 위치는 이와 같이 환경 변수로 저장 가능
        SONAR_SCANNER_HOME = tool name: 'sonar-scanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
    }

    parameters {
        booleanParam(defaultValue: false, description: '배포 포함 여부', name: 'DEPLOY_ENABLED')
    }

    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '5', numToKeepStr: '5')
        githubProjectProperty(displayName: '', projectUrlStr: 'https://github.com/smathj/fastcampus-jenkins')
        // 디폴트 checkout skip 설정 제거
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

    }

    post {
        always {
            scanForIssues tool: ktLint(pattern: '**/ktlint/**/*.xml')
            junit '**/test-results/**/*.xml'
            jacoco sourcePattern: '**/src/main/kotlin'
            script {
                mineRepository()
                emailext attachLog: true, body: email_content(), subject: email_subject(), to: 'smathj007@gmail.com'
                slackSend(channel: "#jenkins", message: "${custom_msg(currentBuild.currentResult)}")
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
                    echo "pipeline-deploy 실행"
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
