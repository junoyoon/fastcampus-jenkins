node {
    properties([
            parameters([
                    gitParameter(branch: '', branchFilter: '.*', defaultValue: 'origin/main', description: '배포할 브랜치를 선택합니다.', listSize: '3', name: 'BRANCH', quickFilterEnabled: true, selectedValue: 'NONE', sortMode: 'ASCENDING_SMART', tagFilter: '*', type: 'GitParameterDefinition'),
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
                    ],
                    booleanParam(defaultValue: true, description: '배포 포함 여부', name: 'INCLUDE_DEPLOY')
            ]),
            pipelineTriggers(),
            buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '5', numToKeepStr: '5')),
            githubProjectProperty(displayName: '', projectUrlStr: 'https://github.com/fastcampus-jenkins/fastcampus-jenkins')
    ])

    stage('CheckOut') { // for display purposes
        // Get some code from a GitHub repository
        checkout scmGit(
                branches: [[name: '${BRANCH}']],
                userRemoteConfigs: [[credentialsId: 'cd6a31dc-9989-4dde-a56b-0247320a727f', url: 'git@github.com:fastcampus-jenkins/fastcampus-jenkins.git']],
                extensions: [[$class: 'SparseCheckoutPaths', sparseCheckoutPaths: [[path: 'projects/spring-app']]]]
        )
    }
    stage('Build') {
        dir("projects/spring-app") {
            withGradle {
               sh "./gradlew build"
           }
        }
    }
    stage('SonarScanner') {
        // sonarqube 환경하에서, 실행
        def sonarScannerHome = tool 'sonar-scanner'

        withSonarQubeEnv("sonarqube-server") {
            sh """
                    ${sonarScannerHome}/bin/sonar-scanner \
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
            def qg = waitForQualityGate()
            if (qg.status != 'OK') {
                error "Pipeline aborted due to quality gate failure: ${qg.status}"
            }
        }
    }

    stage('Results') {
        scanForIssues tool: ktLint(pattern: '**/ktlint/**/*.xml')
        junit '**/test-results/**/*.xml'
        mineRepository()
        jacoco sourcePattern: '**/src/main/kotlin'
        emailext attachLog: true, body: email_content(), subject: email_subject(), to: 'junoyoon@gmail.com'
        slackSend(channel: "#jenkins", message: "${custom_msg(currentBuild.currentResult)}")

        if (currentBuild.result == "SUCCESS") {
            archiveArtifacts artifacts: 'projects/spring-app/build/libs/*-SNAPSHOT.jar', followSymlinks: false
            build(
                    job: 'pipeline-deploy',
                    parameters: [booleanParam(name: 'ARE_YOU_SURE', value: "${env.INCLUDE_DEPLOY}")],
                    wait: false,
                    propagate: false
            )
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
