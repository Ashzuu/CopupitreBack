package ashzuu.project.copupitreback.jpa.entities.user;

/**
 * <h1>Role</h1>
 * <p><strong>Ce que fait la nouvelle classe :</strong> Énumération définissant les différents rôles qu'un utilisateur peut posséder.</p>
 * <p><strong>Dans quel but elle a été créée :</strong> Gérer les autorisations et l'accès aux différentes parties de l'application selon les privilèges de l'utilisateur.</p>
 * <p><strong>À quoi celle-ci est-elle liée :</strong> Elle est liée à la classe User.</p>
 */
public enum Role {
    USER,
    ADMIN
}
