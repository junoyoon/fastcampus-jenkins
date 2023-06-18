pipeline {
    // 어떠한 에이전트에서도 실행 가능함을 표현
    agent any

    options {
        buildDiscarder logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '5', numToKeepStr: '5')
    }

    parameters {
        booleanParam defaultValue: false, description: '정말 배포를 하실건가요?', name: 'ARE_YOU_SURE'
        buildSelector defaultSelector: upstream(fallbackToLastSuccessful: true), description: '배포할 pipeline-build 빌드', name: 'BUILD_SELECTOR'
    }

    // stages > stage > steps 순으로 구성
    stages {
        stage("Checking Deployment") {
            when {
                environment name: 'ARE_YOU_SURE', value: 'false'
            }
            steps {
                error('Executed mistakenly.')
            }
        }

        stage('Copying from upstream.') {
            steps {
                copyArtifacts filter: 'projects/spring-app/build/libs/*', fingerprintArtifacts: true, flatten: true, projectName: 'pipeline', selector: buildParameter('BUILD_SELECTOR'), target: 'deploy'
            }
        }

        stage('Send to target') {
            steps {
                sshPublisher(
                        publishers: [
                                sshPublisherDesc(configName: 'server_1',
                                        transfers: [
                                                sshTransfer(cleanRemote: false, excludes: '', execCommand: 'ls -al', execTimeout: 120000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false, patternSeparator: '[, ]+', remoteDirectory: '', remoteDirectorySDF: false, removePrefix: 'deploy', sourceFiles: 'deploy/*-SNAPSHOT.jar')
                                        ],
                                        usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false)
                        ]
                )
            }
        }
    }
}