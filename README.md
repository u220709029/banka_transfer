🚀 Core Banking Transfer API (Olay Güdümlü Mimari)
Bu proje, modern finansal sistemlerin ihtiyaç duyduğu güvenlik, veri tutarlılığı (ACID) ve asenkron iletişim prensiplerine sadık kalınarak geliştirilmiş, üretime hazır (production-grade) bir bankacılık para transferi API'sidir. Sistem, monolitik bir yapıdan mikroservis mimarisine geçişin temel adımı olan Olay Güdümlü (Event-Driven) bir altyapı üzerine inşa edilmiştir.

Sadece bir CRUD uygulaması olmanın ötesinde; JWT tabanlı güvenlik modeli, Docker container orkestrasyonu, RabbitMQ mesaj kuyruğu entegrasyonu ve GitHub Actions ile tam otomatik CI/CD süreçlerini içeren kapsamlı bir arka uç (backend) mühendisliği vitrinidir.

🏗️ Sistem Mimarisi ve İş Akışı
Sistemdeki para transferi akışı, yüksek erişilebilirlik ve performans göz önünde bulundurularak asenkron bir düzlemde, adım adım şu şekilde tasarlanmıştır:

Yetkilendirme (Authentication): Kullanıcı /api/v1/auth/login uç noktası üzerinden kimliğini doğrular ve sisteme durumsuz (stateless) bir JWT (JSON Web Token) ile erişim sağlar. Bütün kritik uç noktalar Spring Security filtresi ile korunur.

Atomik Transfer İşlemi (Transaction): Gönderen ve alıcı hesapların veritabanındaki bakiye kontrolleri anlık olarak yapılır. Para transferi ve arşiv (geçmiş) kaydı, Spring @Transactional yönetimi altında veritabanına işlenir. Herhangi bir adımda hata oluşursa sistem tam Rollback yaparak veri tutarsızlığını engeller.

Olay Fırlatma (Event Publishing): Transfer başarıyla veritabanına yazıldığı milisaniye, TransferServiceImpl tarafından RabbitMQ'daki banka.exchange dağıtıcısına (Exchange) bir TransferNotificationEvent nesnesi fırlatılır.

Asenkron Tüketim (Event Consumption): API, işlemi tamamlayıp kullanıcıya anında 200 OK dönerken, arka plandaki NotificationListener (Tüketici) kuyruğa düşen mesajı yakalar. Bu yapı sayesinde SMS, E-posta veya Push bildirim süreçleri ana iş parçacığını (main thread) asla bloklamaz.

🛠️ Kullanılan Teknolojiler (Tech Stack)
Sistemin her bir katmanı, kurumsal ölçekteki projelerde tercih edilen endüstri standardı araçlarla kurgulanmıştır:

Backend (Uygulama Katmanı)
Java 17 & Spring Boot 3.x: Ana uygulama çatısı ve bağımlılık yönetimi.

Spring Security & JWT: API uç noktalarının durumsuz (stateless) güvenliği ve rol bazlı erişim kontrolü (RBAC).

Spring Data JPA & Hibernate: Veritabanı işlemleri, nesne-ilişkisel eşleme (ORM) ve Repository kalıbı.

MapStruct: Entity ve DTO nesneleri arasında yüksek performanslı, derleme zamanı (compile-time) veri dönüşümü.

Lombok: Boilerplate (tekrar eden) kodların (Getter, Setter, Builder vb.) minimize edilmesi.

Spring Validation: Dışarıdan gelen IBAN ve bakiye verilerinin HTTP istek katmanında (Controller) sıkı bir şekilde doğrulanması.

Veritabanı & Altyapı
PostgreSQL: İlişkisel veri saklama, veri bütünlüğü ve ACID transaction yönetimi.

Flyway: Veritabanı şema göçleri (migration) ve sürüm kontrolü (V1__init_schema.sql, V2__insert_mock_data.sql). Şema değişiklikleri manuel müdahale gerektirmeden otomatik uygulanır.

Mesajlaşma (Message Broker)
RabbitMQ: Asenkron iletişim, olay güdümlü mimari, Topic Exchange kurgusu ve kuyruk (Queue) yönetimi.

DevOps, Konteynerizasyon & CI/CD
Docker & Docker Compose: Veritabanı, Mesaj Kuyruğu ve Spring Boot uygulamasının birbirleriyle izole bir network içinde, tek bir komutla çoklu konteyner (multi-container) olarak ayağa kaldırılması.

GitHub Actions: Ana dala (main/master) yapılan her kod itme (push) işleminde tetiklenen Sürekli Entegrasyon (CI) boru hattı. Bu sistem sıfır bir Ubuntu sunucu ayağa kaldırır, projeyi derler ve testlerin geçip geçmediğini otonom olarak denetler.

⚙️ Kurulum ve Çalıştırma Rehberi
Projeyi lokal ortamınızda çalıştırmak için iki farklı geliştirme modeli desteklenmektedir. Tercihinize göre aşağıdaki yöntemlerden birini seçebilirsiniz:

Yöntem 1: Tam Konteynerize Çalıştırma (Production / DevOps Yaklaşımı)
Uygulamayı, veritabanını ve RabbitMQ'yu hiçbir yerel bağımlılık kurmadan, tamamen Docker üzerinde izole bir şekilde başlatmak için:

Bash
# Proje dizininde terminali açın ve tüm sistemi inşa edip arka planda başlatın
docker-compose up -d --build

# Uygulamanın ve RabbitMQ'nun canlı loglarını takip etmek için
docker-compose logs -f app
Yöntem 2: Hibrit Geliştirme Modeli (Yazılımcı Yaklaşımı)
Kod geliştirme aşamasında her değişiklikte Docker'ın derlenmesini (rebuild) beklememek için, sadece altyapı servislerini (PostgreSQL ve RabbitMQ) Docker üzerinde tutup, Spring Boot uygulamasını doğrudan IDE'niz üzerinden saniyeler içinde çalıştırabilirsiniz:

Bash
# Sadece veritabanı ve mesaj kuyruğu altyapısını ayağa kaldırın
docker-compose up -d db rabbitmq
Altyapı hazır olduktan sonra IDE'niz üzerinden BankaApplication.java sınıfındaki main metodunu çalıştırın. (Not: Bu yöntemi kullanırken application.properties içindeki veritabanı ve RabbitMQ host ayarlarının localhost olarak yapılandırıldığından emin olun).

📡 API Dokümantasyonu ve Test
Sistem ayağa kalktıktan sonra, projenin sunduğu tüm uç noktaları incelemek ve canlı testler gerçekleştirmek için OpenAPI (Swagger) arayüzünü kullanabilirsiniz:

Swagger UI Adresi: http://localhost:8080/swagger-ui.html

Test Kullanıcısı (Mock Data): gonderen@banka.com

Şifre: 123456

Örnek Kullanım Senaryosu
Swagger arayüzünde POST /api/v1/auth/login uç noktasına gidin.

Yukarıdaki test kullanıcısı bilgileriyle istek atarak JWT token'ınızı alın.

Ekranın sağ üst köşesindeki Authorize (Kilit) ikonuna tıklayarak "Bearer {token}" formatında giriş yapın.

POST /api/v1/transfers uç noktasını kullanarak asenkron para transferi işlemini tetikleyin ve IDE/Docker loglarında RabbitMQ tüketici (consumer) bildirimlerini gözlemleyin.
