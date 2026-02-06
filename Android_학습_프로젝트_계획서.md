# Android 개발 학습 프로젝트 계획서

> 목표: AOS 개발자 포지션 필수 요건 충족을 위한 단계별 프로젝트 수행

---

## Phase 1: 기초 프로젝트 - "할 일 관리 앱 (TodoMate)"

### 1.1 프로젝트 개요

| 항목 | 내용 |
|------|------|
| **목표** | Android 기본 개념 습득 및 필수 요건 기초 충족 |
| **기간** | 2주 |
| **난이도** | ★★☆☆☆ |

### 1.2 학습 목표 (필수 요건 매핑)

| 필수 요건 | 학습 내용 |
|----------|----------|
| Kotlin 기반 Android 앱 | Kotlin 문법, Android 프로젝트 구조 |
| Android View (XML) | Layout XML 작성, View Binding |
| ConstraintLayout | 제약 기반 레이아웃 설계 |
| RecyclerView | 리스트 UI, Adapter 패턴 |
| MVVM | 기초적인 ViewModel 사용 |
| Jetpack | ViewModel, LiveData |
| 비동기 처리 | Coroutines 기초 |

### 1.3 기능 명세

```
[핵심 기능]
├── 할 일 추가/수정/삭제 (CRUD)
├── 할 일 목록 표시 (RecyclerView)
├── 완료 체크 토글
├── 우선순위 설정 (높음/중간/낮음)
├── 카테고리 분류 (업무/개인/쇼핑 등)
└── 로컬 저장 (Room Database)

[추가 기능]
├── 검색 기능
├── 정렬 (날짜/우선순위)
└── 다크 모드 지원
```

### 1.4 기술 스택

```kotlin
// build.gradle.kts (app)
dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.x")

    // Android Jetpack
    implementation("androidx.core:core-ktx:1.12.x")
    implementation("androidx.appcompat:appcompat:1.6.x")
    implementation("androidx.activity:activity-ktx:1.8.x")

    // UI
    implementation("com.google.android.material:material:1.11.x")
    implementation("androidx.constraintlayout:constraintlayout:2.1.x")
    implementation("androidx.recyclerview:recyclerview:1.3.x")

    // Architecture Components
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.x")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.x")

    // Room Database
    implementation("androidx.room:room-runtime:2.6.x")
    implementation("androidx.room:room-ktx:2.6.x")
    kapt("androidx.room:room-compiler:2.6.x")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.x")
}
```

### 1.5 프로젝트 구조

```
app/src/main/
├── java/com/example/todomate/
│   ├── data/
│   │   ├── local/
│   │   │   ├── TodoDatabase.kt
│   │   │   ├── TodoDao.kt
│   │   │   └── TodoEntity.kt
│   │   └── repository/
│   │       └── TodoRepository.kt
│   ├── ui/
│   │   ├── main/
│   │   │   ├── MainActivity.kt
│   │   │   ├── MainViewModel.kt
│   │   │   └── TodoAdapter.kt
│   │   └── detail/
│   │       ├── DetailActivity.kt
│   │       └── DetailViewModel.kt
│   └── util/
│       └── Extensions.kt
├── res/
│   ├── layout/
│   │   ├── activity_main.xml
│   │   ├── activity_detail.xml
│   │   └── item_todo.xml
│   ├── values/
│   │   ├── colors.xml
│   │   ├── strings.xml
│   │   └── themes.xml
│   └── values-night/
│       └── themes.xml
└── AndroidManifest.xml
```

### 1.6 주차별 개발 계획

#### Week 1: 기본 구조 및 UI

| 일차 | 학습/개발 내용 |
|------|---------------|
| Day 1 | Kotlin 기초 (변수, 함수, 클래스, null 안전성) |
| Day 2 | Android 프로젝트 생성, 구조 이해, View Binding 설정 |
| Day 3 | ConstraintLayout으로 메인 화면 레이아웃 작성 |
| Day 4 | RecyclerView + Adapter 구현 (하드코딩 데이터) |
| Day 5 | 할 일 추가 화면 (DetailActivity) 구현 |
| Day 6 | Material Design 컴포넌트 적용 (FAB, Card, Chip) |
| Day 7 | 복습 및 코드 정리 |

#### Week 2: 아키텍처 및 데이터

| 일차 | 학습/개발 내용 |
|------|---------------|
| Day 8 | Room Database 설정 (Entity, DAO) |
| Day 9 | Repository 패턴 구현 |
| Day 10 | ViewModel + LiveData 적용 |
| Day 11 | Coroutines로 비동기 처리 (Room 연동) |
| Day 12 | 수정/삭제 기능 완성 |
| Day 13 | 검색, 정렬, 다크모드 추가 |
| Day 14 | 테스트 및 버그 수정, README 작성 |

### 1.7 핵심 코드 예시

#### MainActivity.kt
```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    private lateinit var todoAdapter: TodoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        observeData()
        setupClickListeners()
    }

    private fun setupRecyclerView() {
        todoAdapter = TodoAdapter(
            onItemClick = { todo -> navigateToDetail(todo) },
            onCheckChanged = { todo -> viewModel.toggleComplete(todo) }
        )
        binding.recyclerView.apply {
            adapter = todoAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }
    }

    private fun observeData() {
        viewModel.todos.observe(this) { todos ->
            todoAdapter.submitList(todos)
            binding.emptyView.isVisible = todos.isEmpty()
        }
    }
}
```

#### MainViewModel.kt
```kotlin
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TodoRepository(
        TodoDatabase.getInstance(application).todoDao()
    )

    val todos: LiveData<List<TodoEntity>> = repository.getAllTodos()

    fun toggleComplete(todo: TodoEntity) {
        viewModelScope.launch {
            repository.update(todo.copy(isCompleted = !todo.isCompleted))
        }
    }

    fun deleteTodo(todo: TodoEntity) {
        viewModelScope.launch {
            repository.delete(todo)
        }
    }
}
```

---

## Phase 2: 숙련 프로젝트 - "영상 스트리밍 앱 (StreamHub)"

### 2.1 프로젝트 개요

| 항목 | 내용 |
|------|------|
| **목표** | 필수 요건 완전 충족 + 우대사항 일부 달성 |
| **기간** | 4주 |
| **난이도** | ★★★★☆ |

### 2.2 학습 목표 (필수 요건 매핑)

| 필수 요건 | 학습 내용 |
|----------|----------|
| ExoPlayer | 영상 재생, 커스텀 컨트롤러 |
| MotionLayout | 화면 전환 애니메이션 |
| MVVM 심화 | 상태 관리 (로딩/성공/실패), UiState 패턴 |
| Jetpack 심화 | Navigation, Hilt, DataStore |
| Flow | StateFlow, SharedFlow, collect |
| REST API 연동 | Retrofit, OkHttp |
| UI 성능 튜닝 | 스크롤 최적화, 이미지 캐싱 |

| 우대사항 | 학습 내용 |
|----------|----------|
| CustomView | 커스텀 비디오 컨트롤러 |
| 복잡한 애니메이션 | MotionLayout 전환 효과 |
| 디자인 시스템 | 재사용 컴포넌트, 스타일 가이드 |

### 2.3 기능 명세

```
[핵심 기능]
├── 영상 목록 (카테고리별)
│   ├── 홈 피드
│   ├── 인기 영상
│   └── 카테고리별 탐색
├── 영상 재생 (ExoPlayer)
│   ├── 재생/일시정지
│   ├── 시간 탐색 (SeekBar)
│   ├── 전체화면 전환
│   ├── 화질 선택
│   └── 배속 조절
├── 영상 상세 페이지
│   ├── 제목, 설명, 조회수
│   ├── 좋아요/싫어요
│   └── 댓글 목록
└── 사용자 기능
    ├── 로그인/로그아웃
    ├── 시청 기록
    └── 좋아요 목록

[고급 기능]
├── PIP (Picture-in-Picture) 모드
├── 오프라인 저장 (다운로드)
├── 백그라운드 재생
└── 연속 재생 (Autoplay)
```

### 2.4 기술 스택

```kotlin
// build.gradle.kts (app)
dependencies {
    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.x")

    // Android Jetpack
    implementation("androidx.core:core-ktx:1.12.x")
    implementation("androidx.appcompat:appcompat:1.6.x")
    implementation("androidx.activity:activity-ktx:1.8.x")
    implementation("androidx.fragment:fragment-ktx:1.6.x")

    // UI
    implementation("com.google.android.material:material:1.11.x")
    implementation("androidx.constraintlayout:constraintlayout:2.1.x")
    implementation("androidx.recyclerview:recyclerview:1.3.x")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.x")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.x")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.x")

    // Lifecycle & ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.x")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.x")

    // ExoPlayer (Media3)
    implementation("androidx.media3:media3-exoplayer:1.2.x")
    implementation("androidx.media3:media3-ui:1.2.x")
    implementation("androidx.media3:media3-session:1.2.x")

    // Network
    implementation("com.squareup.retrofit2:retrofit:2.9.x")
    implementation("com.squareup.retrofit2:converter-gson:2.9.x")
    implementation("com.squareup.okhttp3:okhttp:4.12.x")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.x")

    // Image Loading
    implementation("io.coil-kt:coil:2.5.x")

    // Dependency Injection
    implementation("com.google.dagger:hilt-android:2.48.x")
    kapt("com.google.dagger:hilt-compiler:2.48.x")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.x")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.x")
}
```

### 2.5 프로젝트 구조 (Clean Architecture)

```
app/src/main/
├── java/com/example/streamhub/
│   ├── di/                          # Hilt 모듈
│   │   ├── NetworkModule.kt
│   │   ├── DatabaseModule.kt
│   │   └── RepositoryModule.kt
│   ├── data/
│   │   ├── remote/
│   │   │   ├── api/
│   │   │   │   └── VideoApi.kt
│   │   │   └── dto/
│   │   │       └── VideoDto.kt
│   │   ├── local/
│   │   │   ├── db/
│   │   │   └── datastore/
│   │   └── repository/
│   │       └── VideoRepositoryImpl.kt
│   ├── domain/
│   │   ├── model/
│   │   │   └── Video.kt
│   │   ├── repository/
│   │   │   └── VideoRepository.kt
│   │   └── usecase/
│   │       ├── GetVideosUseCase.kt
│   │       └── GetVideoDetailUseCase.kt
│   ├── presentation/
│   │   ├── common/
│   │   │   ├── UiState.kt
│   │   │   └── BaseViewModel.kt
│   │   ├── home/
│   │   │   ├── HomeFragment.kt
│   │   │   ├── HomeViewModel.kt
│   │   │   └── VideoAdapter.kt
│   │   ├── player/
│   │   │   ├── PlayerActivity.kt
│   │   │   ├── PlayerViewModel.kt
│   │   │   └── CustomPlayerView.kt    # CustomView
│   │   └── detail/
│   │       ├── DetailFragment.kt
│   │       └── DetailViewModel.kt
│   └── util/
│       ├── Extensions.kt
│       └── Constants.kt
├── res/
│   ├── layout/
│   ├── xml/
│   │   └── motion_scene_player.xml   # MotionLayout
│   ├── navigation/
│   │   └── nav_graph.xml
│   └── values/
└── AndroidManifest.xml
```

### 2.6 주차별 개발 계획

#### Week 1: 프로젝트 설정 및 네트워크

| 일차 | 학습/개발 내용 |
|------|---------------|
| Day 1 | Clean Architecture 개념 학습, 프로젝트 구조 설계 |
| Day 2 | Hilt 설정, DI 모듈 작성 |
| Day 3 | Retrofit + OkHttp 설정, API 인터페이스 정의 |
| Day 4 | Repository 패턴 구현 (Remote + Local) |
| Day 5 | UseCase 레이어 구현 |
| Day 6 | 홈 화면 UI (RecyclerView + 다중 ViewType) |
| Day 7 | 복습 및 코드 리팩토링 |

#### Week 2: 상태 관리 및 Navigation

| 일차 | 학습/개발 내용 |
|------|---------------|
| Day 8 | UiState 패턴 설계 (Loading/Success/Error) |
| Day 9 | StateFlow + collect 적용 |
| Day 10 | Navigation Component 설정 |
| Day 11 | 화면 간 데이터 전달 (SafeArgs) |
| Day 12 | 영상 상세 화면 구현 |
| Day 13 | 에러 핸들링 + Retry 로직 |
| Day 14 | 복습 및 테스트 |

#### Week 3: ExoPlayer 및 영상 재생

| 일차 | 학습/개발 내용 |
|------|---------------|
| Day 15 | ExoPlayer (Media3) 기초, 기본 재생 구현 |
| Day 16 | 커스텀 컨트롤러 UI 작성 |
| Day 17 | CustomView로 PlayerView 확장 |
| Day 18 | 전체화면 전환, 화면 회전 처리 |
| Day 19 | 화질 선택, 배속 조절 기능 |
| Day 20 | 백그라운드 재생 (MediaSession) |
| Day 21 | 복습 및 버그 수정 |

#### Week 4: 애니메이션 및 최적화

| 일차 | 학습/개발 내용 |
|------|---------------|
| Day 22 | MotionLayout 기초, 전환 애니메이션 |
| Day 23 | 영상 상세 → 미니 플레이어 전환 구현 |
| Day 24 | 이미지 캐싱 최적화 (Coil) |
| Day 25 | RecyclerView 스크롤 성능 튜닝 |
| Day 26 | 메모리 누수 점검 (LeakCanary) |
| Day 27 | 최종 테스트 및 QA |
| Day 28 | README 작성, 포트폴리오 정리 |

### 2.7 핵심 코드 예시

#### UiState.kt (상태 관리)
```kotlin
sealed interface UiState<out T> {
    object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String, val retry: (() -> Unit)? = null) : UiState<Nothing>
}
```

#### HomeViewModel.kt (Flow 사용)
```kotlin
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getVideosUseCase: GetVideosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Video>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Video>>> = _uiState.asStateFlow()

    init {
        loadVideos()
    }

    fun loadVideos() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading

            getVideosUseCase()
                .catch { e ->
                    _uiState.value = UiState.Error(
                        message = e.message ?: "알 수 없는 오류",
                        retry = { loadVideos() }
                    )
                }
                .collect { videos ->
                    _uiState.value = UiState.Success(videos)
                }
        }
    }
}
```

#### HomeFragment.kt (상태 처리)
```kotlin
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is UiState.Loading -> showLoading()
                        is UiState.Success -> showContent(state.data)
                        is UiState.Error -> showError(state.message, state.retry)
                    }
                }
            }
        }
    }

    private fun showLoading() {
        binding.progressBar.isVisible = true
        binding.recyclerView.isVisible = false
        binding.errorView.isVisible = false
    }

    private fun showContent(videos: List<Video>) {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = true
        binding.errorView.isVisible = false
        adapter.submitList(videos)
    }

    private fun showError(message: String, retry: (() -> Unit)?) {
        binding.progressBar.isVisible = false
        binding.recyclerView.isVisible = false
        binding.errorView.isVisible = true
        binding.errorMessage.text = message
        binding.retryButton.setOnClickListener { retry?.invoke() }
    }
}
```

#### PlayerActivity.kt (ExoPlayer)
```kotlin
class PlayerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlayerBinding
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                binding.playerView.player = exoPlayer

                val videoUrl = intent.getStringExtra(EXTRA_VIDEO_URL) ?: return
                val mediaItem = MediaItem.fromUri(videoUrl)

                exoPlayer.setMediaItem(mediaItem)
                exoPlayer.prepare()
                exoPlayer.playWhenReady = true
            }
    }

    override fun onStart() {
        super.onStart()
        initializePlayer()
    }

    override fun onStop() {
        super.onStop()
        releasePlayer()
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            exoPlayer.release()
        }
        player = null
    }
}
```

---

## 학습 리소스

### 공식 문서
- [Android Developers](https://developer.android.com/)
- [Kotlin 공식 문서](https://kotlinlang.org/docs/home.html)
- [ExoPlayer (Media3)](https://developer.android.com/guide/topics/media/media3)

### 추천 강의
- [Android Basics with Compose](https://developer.android.com/courses/android-basics-compose/course) (무료)
- [Now in Android](https://github.com/android/nowinandroid) - Google 공식 샘플 앱

### 참고 오픈소스
- [architecture-samples](https://github.com/android/architecture-samples)
- [sunflower](https://github.com/android/sunflower)

---

## 포트폴리오 활용

### GitHub README 구성
```markdown
# 프로젝트명

## 스크린샷/데모 GIF

## 주요 기능

## 기술 스택

## 아키텍처
- MVVM + Clean Architecture
- UiState 패턴

## 학습 내용
- [상세 내용]

## 트러블슈팅
- [문제 → 해결 과정]
```

### 면접 대비 포인트
1. **왜 MVVM을 선택했는가?**
2. **Flow vs LiveData 차이점**
3. **ExoPlayer 메모리 관리 방법**
4. **RecyclerView 최적화 경험**
5. **MotionLayout 사용 시 주의점**

---

## 일정 요약

| Phase | 프로젝트 | 기간 | 목표 |
|-------|---------|------|------|
| 1 | TodoMate (기초) | 2주 | 필수 요건 기초 습득 |
| 2 | StreamHub (숙련) | 4주 | 필수 요건 완전 충족 + 우대사항 |
| **총** | | **6주** | AOS 포지션 지원 가능 수준 |

---

*작성일: 2026-01-27*
