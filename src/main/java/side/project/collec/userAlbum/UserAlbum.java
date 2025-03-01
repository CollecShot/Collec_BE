//package side.project.collec.userAlbum;
//
//import jakarta.persistence.*;
//import lombok.AllArgsConstructor;
//import lombok.Data;
//import lombok.NoArgsConstructor;
//import side.project.collec.album.Album;
//import side.project.collec.user.User;
//
//@Entity
//@Table(name = "user_album")
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//public class UserAlbum {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "id", updatable = false)
//    private Long id;
//
//    @Column(name = "album_name", nullable = false)
//    private String albumName;
//
//    @OneToOne(mappedBy = "userAlbum", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private User user;
//
//    @ManyToOne
//    @JoinColumn(name = "album_id", nullable = false)
//    private Album album;
//}
