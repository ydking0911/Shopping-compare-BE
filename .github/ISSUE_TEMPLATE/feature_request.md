---
name: Feature request
about: Feature Issue Template
title: "[FEAT]"
labels: ''
assignees: ''

---

name: ✨ 기능 요청
description: 새로운 기능이나 개선을 제안합니다
labels: [ "enhancement" ]

body:
  - type: markdown
    attributes:
      value: |
        중복 이슈가 없는지 먼저 검색해 주세요. 핵심만 간단히 적어주시면 됩니다.

  - type: input
    id: summary
    attributes:
      label: 요약
      description: 한두 줄로 제안을 요약해 주세요
      placeholder: 예) 대시보드에 실시간 검색 로그 위젯 추가
    validations:
      required: true

  - type: textarea
    id: problem
    attributes:
      label: 문제/목표
      description: 왜 필요한가요? 어떤 가치를 기대하나요?
      placeholder: 예) 현재 ○○ 과정이 느려서… / 사용자로서 …을 하고 싶다
    validations:
      required: true

  - type: textarea
    id: proposal
    attributes:
      label: 제안 내용
      description: 원하는 동작이나 간단한 설계를 적어주세요(가능하면 스크린샷/목업 링크)
      placeholder: 예) 버튼 추가 위치, 간단한 플로우, API 변경 요약 등
    validations:
      required: true

  - type: textarea
    id: acceptance
    attributes:
      label: 수용 기준(선택)
      description: 완료 여부를 판단할 수 있는 체크리스트
      placeholder: |
        - [ ] 목록 화면에서 2초 내 렌더링
        - [ ] 권한 없는 사용자는 버튼 미노출

  - type: textarea
    id: references
    attributes:
      label: 참고 자료(선택)
      description: 관련 이슈/PR, 문서, 목업 링크 등
      placeholder: 예) #123, Figma 링크, 경쟁사 예시
