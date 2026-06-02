# JavaProgramging_termProject

Workout Routine Manager (웬들러 531 기반 운동 루틴 관리 프로그램)

## 1. Overview
웬들러 531 운동 프로그램을 기반으로, 사용자가 1RM(최대 중량)을 입력하면 워밍업, 본세트, PR세트 중량을 자동 계산해주는 GUI 기반 자바 프로그램을 구현한다. (Java Swing 사용)

## 2. 목표
- 사용자로부터 4대 운동(스쿼트, 데드리프트, 벤치프레스, 오버헤드프레스)의 1RM을 입력받는다.
- TM(Training Max) = 1RM × 90%를 자동 계산한다.
- 주차별(1~3주차) 워밍업·본세트·PR세트 중량을 계산하여 출력한다.
- 선택한 보조 볼륨(FSL / SST / BBB) 중량도 함께 출력한다.

## 3. 구현 기능
- 1RM 입력 (필수)
  - `JTextField` 4개를 사용해 스쿼트, 데드리프트, 벤치프레스, 오버헤드프레스의 1RM을 입력받는다.
  - 각 입력 필드 옆에 `JLabel`로 운동 이름과 운동 이미지를 표시한다.
  - 이미지는 위키미디어(`upload.wikimedia.org`) URL에서 `new ImageIcon(URL)`로 로드하고 `getScaledInstance()`로 64×64 크기로 축소한다.
  - GUI 블로킹을 피하기 위해 별도 `Thread`에서 비동기 로드하고, 완료 후 `SwingUtilities.invokeLater()`로 `JLabel`에 적용한다.
- TM 자동 계산 (필수)
  - 입력받은 1RM에 0.9를 곱해 TM을 계산한다.
- 워밍업 세트 (필수)
  - TM의 40%, 50%, 60%로 워밍업 세트(5/5/3회)를 계산하여 각 종목의 결과에 표시한다.
- 주차별 본세트 계산 (필수)
  - TM을 기반으로 1주차(5/5/5+), 2주차(3/3/3+), 3주차(5/3/1+) 세트 중량을 계산한다.
- 7주 사이클 / 디로딩 (필수)
  - `JComboBox`로 전3주(1~3주) / 후3주(4~6주) / 디로딩(7주) 단계를 선택한다.
  - 후3주와 디로딩 단계는 TM에 증량을 더한 값을 사용한다 (벤치·프레스 +2.5kg, 스쿼트·데드 +5kg).
  - 디로딩 단계에서는 본세트 대신 40/50/60% × 5/5/5 패턴을 출력하고, 보조 볼륨은 생략한다.
- 중량 반올림 (필수)
  - 계산된 중량을 2.5kg 단위로 반올림한다.
- 결과 출력 (필수)
  - 4개 운동의 결과를 각각 `LiftPanel`(JPanel 서브클래스)로 표시하고, 메인 결과 영역은 `GridLayout(2, 2)`로 4개 패널을 배치한다.
  - 각 `LiftPanel`은 운동 이름(TitledBorder), TM 값과 현재 사이클 단계, 워밍업 행, 본세트(또는 디로딩) 3행, 보조 볼륨 라벨을 포함한다.
- 보조 볼륨 선택 및 출력 (선택)
  - `JComboBox`로 FSL / SSL / BBB 중 보조 볼륨을 선택하면 결과에 함께 출력된다 (디로딩 주차 제외).
- 파일 저장 / 불러오기 (필수)
  - `File` 메뉴의 "저장" / "불러오기"로 현재 1RM, 보조 볼륨, 사이클 단계를 CSV 파일(`workout_data.csv`)에 저장하거나 읽어온다.
  - 프로그램 시작 시 저장된 파일이 있으면 자동으로 불러온다.
  - 자바 표준 I/O(`FileWriter` / `BufferedReader`)만 사용한다.
- GUI 구성 (필수)
  - `JFrame`을 상속한 메인 윈도우에 입력 패널(North) / 결과 영역(Center) / 컨트롤 패널(South)을 `BorderLayout`으로 배치한다.
  - 입력 패널 내부는 `GridLayout(4, 2)`로 운동 이름 라벨과 입력창을 정렬한다.
  - "계산" 버튼(`JButton`)을 누르면 `ActionListener`가 동작해 결과를 갱신한다.
- 메뉴 (필수)
  - `JMenuBar`에 "File"과 "기록" 메뉴를 둔다.
  - File: 저장 / 불러오기 / 초기화 / 종료
  - 기록: 기록 추가 / 추정 1RM, 기록 보기
- 휴식 타이머 (필수)
  - `Thread`를 사용해 세트 간 휴식 시간을 카운트다운(60/90/120/180/300초 선택).
  - `JProgressBar`로 진행률, 남은 시간 라벨, 시작/정지 버튼 제공.
  - 완료 시 `Toolkit.getDefaultToolkit().beep()`으로 알림.
- PR 세트 추정 1RM 계산 (필수)
  - AMRAP 세트에서 달성한 무게와 reps를 입력하면 Epley 공식(`1RM ≈ w × (1 + r/30)`)으로 새 1RM을 추정한다.
  - "1RM에 반영" 버튼으로 입력칸을 자동 갱신한다.
- 운동 기록 (필수)
  - 날짜·운동·무게·reps·추정 1RM을 `workout_history.csv`에 append.
  - "기록 보기" 다이얼로그에서 `JTable`로 전체 기록을 표 형태로 출력.

## 4. 구현 계획

### 4.1 패키지 / 클래스 구성
강의자료(`MyApp` / `MyFrame` 예제)의 구조를 따라 진입점 클래스와 `JFrame` 클래스를 분리한다.

- `WorkoutApp.java` : `main()` 진입점. `new WorkoutFrame()` 호출만 담당.
- `WorkoutFrame.java` : `extends JFrame`. 생성자에서 `setTitle / setSize / setDefaultCloseOperation / setVisible` 설정, `getContentPane()`에 패널 배치, 이벤트 리스너 등록, 시작 시 저장 파일 자동 로드.
- `WorkoutCalculator.java` : TM·워밍업·주차별 본세트·디로딩·보조 볼륨·TM 증량 계산 로직 (GUI 비의존, 순수 계산 클래스).
- `CyclePhase.java` : 사이클 단계 enum (`FIRST_THREE`, `LAST_THREE`, `DELOAD`). TM 증량 여부, 디로딩 여부, 시작 주차 번호 등의 헬퍼 메서드 제공.
- `LiftPanel.java` : `extends JPanel`. 한 종목의 TM/워밍업/본세트(또는 디로딩)/보조 볼륨을 `JLabel`과 `GridLayout`으로 표시하고, `update()` 호출로 값을 갱신한다.
- `WorkoutStorage.java` : CSV 파일 저장/불러오기. 내부 `State` 클래스로 1RM·보조 볼륨·사이클 단계를 묶어 `FileWriter`/`BufferedReader`로 처리.
- `WorkoutHistory.java` : 운동 기록을 `workout_history.csv`에 append/load. 내부 `Entry` 클래스(날짜·운동·무게·reps·추정 1RM).
- `RestTimer.java` : `implements Runnable`. 별도 `Thread`에서 카운트다운을 수행하며 `SwingUtilities.invokeLater()`로 UI 콜백 호출. 내부 `TimerListener` 인터페이스로 onTick/onFinish 알림.
- `TimerPanel.java` : 휴식 타이머 UI. `JComboBox`(시간 선택), `JButton`(시작/정지), `JLabel`(남은 시간), `JProgressBar`로 구성. `RestTimer.TimerListener` 구현.
- `AddRecordDialog.java` : `JDialog` 기반 기록 추가/추정 1RM 다이얼로그. 운동·무게·reps 입력 → 추정 1RM 표시 → 저장 또는 1RM 반영.
- `HistoryDialog.java` : `JDialog` + `JTable`로 운동 기록 전체를 표로 출력.

### 4.2 GUI 구성 (강의자료 패턴 적용)
- **윈도우**: `WorkoutFrame extends JFrame`
  ```
  setTitle("Workout Routine Manager");
  setSize(500, 600);
  setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
  Container c = getContentPane();
  c.setLayout(new BorderLayout());
  setVisible(true);
  ```
- **레이아웃 배치**:
  - North: 입력 패널 (`JPanel` + `GridLayout(4,2)`) — 4개 운동 라벨 + 4개 `JTextField`
  - Center: 결과 패널 (`JPanel` + `GridLayout(2,2)`) — 4개 `LiftPanel`
  - South: South 래퍼 (`BorderLayout`) — North에 `TimerPanel`, South에 컨트롤 패널(사이클 `JComboBox`, 보조 볼륨 `JComboBox`, "계산" / "초기화" `JButton`)
- **메뉴**: `JMenuBar` → "File" / "기록"
  - File → 저장 / 불러오기 / 초기화 / 종료
  - 기록 → 기록 추가 / 추정 1RM, 기록 보기
- **이벤트 처리**: 강의자료의 3가지 방식 중 **Inner Class** 또는 **Anonymous Class** 방식을 사용한다.
  ```
  calcButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
          // 1RM 읽기 → WorkoutCalculator 호출 → 각 LiftPanel.update()로 라벨 갱신
      }
  });
  ```

### 4.3 핵심 계산 공식
- TM = 1RM × 0.9
- 워밍업: TM × 40%, 50%, 60% (5/5/3회)
- 1주차 본세트: TM × 65%, TM × 75%, TM × 85% (5/5/5+)
- 2주차 본세트: TM × 70%, TM × 80%, TM × 90% (3/3/3+)
- 3주차 본세트: TM × 75%, TM × 85%, TM × 95% (5/3/1+)
- 디로딩 (7주차): TM × 40%, 50%, 60% (5/5/5)
- TM 증량 (후3주 시작 전): 벤치·프레스 +2.5kg, 스쿼트·데드 +5kg
- FSL = 1주차 본세트 1의 중량으로 8회 5세트
- SSL = 본세트2 중량으로 8회 5세트 (스쿼트·데드는 4회 5세트)
- BBB = TM × 50%로 10회 5세트 (스쿼트·데드는 5회 5세트)

### 4.4 7주 사이클 구조
- 전3주(1~3주차) → TM 증량 → 후3주(4~6주차) → 디로딩(7주차) 순으로 진행된다.
- 전3주가 끝나고 후3주 시작 전에 벤치·프레스는 2.5kg, 스쿼트·데드는 5kg TM을 올린다. 디로딩 후에도 동일하게 증량한다.
- 디로딩 주차는 중량을 낮추고 볼륨을 줄여 회복에 집중한다.

## 5. PWA 버전 (모바일용)

같은 기능을 휴대폰에서 쓸 수 있도록 `pwa/` 폴더에 웹 버전을 포함했다. 순수 HTML/CSS/JS로 구현되어 있고, 서비스 워커로 오프라인 동작과 홈 화면 추가를 지원한다.

### 5.1 파일 구성
- `pwa/index.html` : 메인 페이지 (입력·결과·타이머·기록 모달)
- `pwa/style.css` : 다크 테마 + 모바일 세로 화면 우선 레이아웃
- `pwa/app.js` : 계산 로직(JS 포팅), 입력 토글, localStorage 저장, 휴식 타이머, Epley 추정, JSON 백업
- `pwa/manifest.json`, `pwa/sw.js` : PWA 매니페스트 + 오프라인 캐시
- `pwa/icons/` : 앱 아이콘 (SVG + 192/512 PNG + 512 maskable)
- `pwa/images/` : 운동 이미지(스쿼트·데드·벤치·프레스)

### 5.2 로컬에서 실행
서비스 워커는 `file://`에서 동작하지 않으므로 간단한 HTTP 서버가 필요하다.

```
cd pwa
python3 -m http.server 8765
```

브라우저에서 `http://localhost:8765` 를 연다.

### 5.3 휴대폰에 설치
1. PC와 같은 Wi-Fi 네트워크에서 위 명령으로 서버를 띄운다.
2. PC IP 주소를 확인한다 (`ipconfig getifaddr en0`).
3. 폰 브라우저에서 `http://<PC-IP>:8765` 로 접속한다.
4. **Android(Chrome)**: 주소창 우측 메뉴 → "홈 화면에 추가".
5. **iPhone(Safari)**: 공유 버튼 → "홈 화면에 추가".

이후에는 오프라인에서도 열리며, 입력값·기록은 휴대폰 브라우저에 저장된다. 백업이 필요하면 메뉴(⋯)에서 JSON으로 내보내기/가져오기를 사용한다.

### 5.4 영구 호스팅 (선택)
`pwa/` 폴더 내용을 GitHub Pages 등 정적 호스팅에 그대로 올리면 인터넷이 되는 어디서나 접근 가능하다. 모든 데이터는 클라이언트 측 localStorage에만 저장되어 외부로 전송되지 않는다.
