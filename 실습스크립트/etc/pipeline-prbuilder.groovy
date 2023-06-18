// 원래 parameters 는 stages 앞쪽에 선언적으로 배치하는 것이 가이드 되나,
// 명시적으로 선언적 pipeline 문법이 제공되지 않은 플러그도 선언적 pipeline 에 적용하기 위해 groovy script 로 작성
// 배치된 순서에 따라 노출이 되므로, parameters 에 적용할 수 있었던 gitParameter 와 booleanParam 도 적용

properties([
        pipelineTriggers([
                // PR builder trigger
                [
                        $class        : 'GhprbTrigger',
                        adminlist     : 'junoyoon',
                        cron: "*/15 * * * *",
                        permitAll     : false,
                        useGitHubHooks: true,
                        triggerPhrase : 'test this',
                        gitHubAuthId  : '0e70019f-38ed-4341-aaaa-1218d4d3754f',
                        extensions    : [
                                [
                                        $class             : 'GhprbSimpleStatus',
                                        commitStatusContext: 'Jenkins',
                                        showMatrixStatus   : false
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

    triggers {
        pollSCM '*/10 * * * *' // Poll Scm
        githubPush() // GitHub hook trigger for GITScm polling
    }

    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '5', numToKeepStr: '5')
        githubProjectProperty(displayName: '', projectUrlStr: 'https://github.com/fastcampus-jenkins/fastcampus-jenkins')
    }


    // stages > stage > steps 순으로 구성
    stages {
        stage('Checkout') {
            steps {
                echo "빌드할 커밋 HASH ${SHA1}"

                checkout scmGit(
                        branches: [[name: '${SHA1}']],
                        userRemoteConfigs: [[credentialsId: 'cd6a31dc-9989-4dde-a56b-0247320a727f', url: 'git@github.com:fastcampus-jenkins/fastcampus-jenkins.git']],
                        extensions: [[$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[path: 'projects/spring-app']]]]
                )
            }
        }

        stage('Build') {
            steps {
                // withGradle 을 하면, Gradle 로그를 해석
                dir("projects/spring-app") {
                    withGradle {
                        sh "./gradlew build --console=plain"
                    }
                }
                echo "echo ${env.ghprbCommentBody}"
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
    }

    // post 는 stage 마다 실행시킬 수도 있고, 전체가 stages가 완료된 다음에 실행 시킬 수도 있음
    post {
        always {
            scanForIssues tool: ktLint(pattern: '**/ktlint/**/*.xml')
            junit '**/test-results/**/*.xml'
            jacoco sourcePattern: '**/src/main/kotlin'
        }
    }
}