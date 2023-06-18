// 원래 parameters 는 stages 앞쪽에 선언적으로 배치하는 것이 가이드 되나,
// 명시적으로 선언적 pipeline 문법이 제공되지 않은 플러그도 선언적 pipeline 에 적용하기 위해 groovy script 로 작성
// 배치된 순서에 따라 노출이 되므로, parameters 에 적용할 수 있었던 gitParameter 와 booleanParam 도 적용
//
properties([
        parameters([
                [
                        $class              : 'DynamicReferenceParameter',
                        choiceType          : 'ET_FORMATTED_HTML',
                        name                : 'BRANCH_TO_BUILD',
                        referencedParameters: 'BRANCH',
                        script              : [
                                $class        : 'GroovyScript',
                                script        : [
                                        sandbox: true,
                                        script : groovy_script()
                                ],
                                fallbackScript: [
                                        sandbox: true,
                                        script : 'return ""'
                                ]
                        ]
                ]

        ])
])

pipeline {
    // 어떠한 에이전트에서도 실행 가능함을 표현
    agent any
    environment {
        // jenkins 가 관리하는 도구의 위치는 이와 같이 환경 변수로 저장 가능
        SONAR_SCANNER_HOME = tool name: 'sonar-scanner', type: 'hudson.plugins.sonar.SonarRunnerInstallation'
    }

    parameters {
            gitParameter(branch: '', branchFilter: '.*', defaultValue: 'origin/main', description: '배포할 브랜치를 선택합니다.', listSize: '3', name: 'BRANCH', quickFilterEnabled: true, selectedValue: 'NONE', sortMode: 'ASCENDING_SMART', tagFilter: '*', type: 'GitParameterDefinition')
            booleanParam(defaultValue: true, description: '배포 포함 여부', name: 'INCLUDE_DEPLOY')
    }
    triggers {
        pollSCM '*/10 * * * *' // Poll Scm
        // githubPush() // GitHub hook trigger for GITScm polling
    }

    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '5', numToKeepStr: '5')
        githubProjectProperty(displayName: '', projectUrlStr: 'https://github.com/fastcampus-jenkins/fastcampus-jenkins')
    }


    // stages > stage > steps 순으로 구성
    stages {
        stage('Checkout') {
            steps {
                // 파라미터는 env.BRANCH, params.BRANCH, BRANCH 세가지 방식으로 접근 가능
                // 환경 변수는 env.ENV, ENV 로 접근 가능

                echo "WORKSPACE : ${env.WORKSPACE}, GIT BRANCH: ${env.GIT_BRANCH}"
                echo "빌드할 브랜치  : BRANCH ${BRANCH}, env.BRANCH ${env.BRANCH}, params.BRANCH ${params.BRANCH}"
                echo "배포 포함 여부 : INCLUDE_DEPLOY  ${INCLUDE_DEPLOY}, env.INCLUDE_DEPLOY ${env.INCLUDE_DEPLOY}, params.INCLUDE_DEPLOY ${params.INCLUDE_DEPLOY}"

                checkout scmGit(
                        branches: [[name: "${BRANCH ?: GIT_BRANCH}"]],
                        userRemoteConfigs: [[credentialsId: 'cd6a31dc-9989-4dde-a56b-0247320a727f', url: 'git@github.com:fastcampus-jenkins/fastcampus-jenkins.git']],
                        extensions: [[$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[path: 'projects/spring-app']]]]
                )
                echo "WORKSPACE : ${env.WORKSPACE}, GIT BRANCH: ${env.GIT_BRANCH}"
            }
        }

        stage('Build') {
            steps {
                // withGradle 을 하면, Gradle 로그를 해석
                dir("projects/spring-app") {
                    withGradle {
                        sh "./gradlew build"
                    }
                }
                echo "빌드한 브랜치  : BRANCH ${BRANCH}, env.BRANCH ${env.BRANCH}, params.BRANCH ${params.BRANCH}"
                echo "배포 포함 여부 : INCLUDE_DEPLOY  ${INCLUDE_DEPLOY}, env.INCLUDE_DEPLOY ${env.INCLUDE_DEPLOY}, params.INCLUDE_DEPLOY ${params.INCLUDE_DEPLOY}"
            }
        }

        stage('SonarScanner') {
            steps {
                // sonarqube 환경하에서, 실행
                withSonarQubeEnv("sonarqube-server") {
                    sh """
                    ${env.SONAR_SCANNER_HOME}/bin/sonar-scanner \
                        -Dsonar.host.url=http://sonarqube:9000 \
                        -Dsonar.projectKey=sample \
                        -Dsonar.projectBaseDir=${WORKSPACE}/projects/spring-app \
                        -Dsonar.login=sqp_62516f0201f5180e699618182af349aacf2591ff
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

        stage('Deploy') {
//            input {
//                    message '배포를 승인하시나요?'
//                    ok '예'
//                    parameters {
//                        booleanParam defaultValue: true, name: 'ALLOW_TO_DEPLOY'
//                    }
//            }
//            when {
//                environment name: 'ALLOW_TO_DEPLOY', value: 'true'
//            }

            when {
                environment name: 'INCLUDE_DEPLOY', value: 'true'
            }
            steps {
                build(
                        job: 'pipeline-deploy',
                        parameters: [booleanParam(name: 'ARE_YOU_SURE', value: "${env.INCLUDE_DEPLOY}")],
                        wait: false,
                        propagate: false
                )
            }
        }
    }

    // post 는 stage 마다 실행시킬 수도 있고, 전체가 stages가 완료된 다음에 실행 시킬 수도 있음
    post {
        always {
            scanForIssues tool: ktLint(pattern: '**/ktlint/**/*.xml')
            junit '**/test-results/**/*.xml'
            jacoco sourcePattern: '**/src/main/kotlin'
            mineRepository()
            emailext attachLog: true, body: email_content(), subject: email_subject(), to: 'junoyoon@gmail.com'
            slackSend(channel: "#jenkins", message: "${custom_msg(currentBuild.currentResult)}")
        }
        success {
            archiveArtifacts artifacts: 'projects/spring-app/build/libs/*-SNAPSHOT.jar', followSymlinks: false
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

def groovy_script() {
        return '''if (BRANCH == "") {
            return "origin/main 가 빌드 됩니다."
        } else {
            return "$BRANCH 가 빌드 됩니다."
        }'''
}

def groovy_script_for_jenkinsfile() {
    return '''
        def replaced = BRANCH.replace("origin/", "")
        if (replaced == "") {
            return "<input name='value' value='main' class='setting-input' type='text'>"
        } else {
            return "<input name='value' value='${replaced}' class='setting-input' type='text'>"
        }'''
}

def groovy_fallback_script_for_jenkinsfile() {
    return '''
       return "<input name='value' value='main' class='setting-input' type='text'>"
    '''
}

def custom_msg(status) {
    return " $status: Job [${env.JOB_NAME}] Logs path: ${env.BUILD_URL}/consoleText"
}