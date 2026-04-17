package edu.teamcandy.repositories

import edu.teamcandy.models.Movie

class MovieRepository {
    private val movies = listOf(
        Movie(
            1, "The Shawshank Redemption", 142, "R",
            listOf("Tim Robbins", "Morgan Freeman"), listOf("Drama"),
            "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency."
        ),
        Movie(
            2, "The Godfather", 175, "R",
            listOf("Marlon Brando", "Al Pacino"), listOf("Crime", "Drama"),
            "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son."
        ),
        Movie(
            3, "The Dark Knight", 152, "PG-13",
            listOf("Christian Bale", "Heath Ledger"), listOf("Action", "Crime", "Drama"),
            "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice."
        ),
        Movie(
            4, "Pulp Fiction", 154, "R",
            listOf("John Travolta", "Uma Thurman", "Samuel L. Jackson"), listOf("Crime", "Drama"),
            "The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption."
        ),
        Movie(
            5, "Inception", 148, "PG-13",
            listOf("Leonardo DiCaprio", "Joseph Gordon-Levitt"), listOf("Action", "Adventure", "Sci-Fi"),
            "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O."
        ),
        Movie(
            6, "Forrest Gump", 142, "PG-13",
            listOf("Tom Hanks", "Robin Wright"), listOf("Drama", "Romance"),
            "The presidencies of Kennedy and Johnson, the Vietnam War, the Watergate scandal and other historical events unfold from the perspective of an Alabama man with an IQ of 75, whose only desire is to be reunited with his childhood sweetheart."
        ),
        Movie(
            7, "Fight Club", 139, "R",
            listOf("Brad Pitt", "Edward Norton"), listOf("Drama"),
            "An insomniac office worker and a devil-may-care soap maker form an underground fight club that evolves into something much, much more."
        ),
        Movie(
            8, "The Matrix", 136, "R",
            listOf("Keanu Reeves", "Laurence Fishburne"), listOf("Action", "Sci-Fi"),
            "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers."
        ),
        Movie(
            9, "Goodfellas", 145, "R",
            listOf("Robert De Niro", "Ray Liotta"), listOf("Biography", "Crime", "Drama"),
            "The story of Henry Hill and his life in the mob, covering his relationship with his wife Karen Hill and his mob partners Jimmy Conway and Tommy DeVito in the Italian-American crime syndicate."
        ),
        Movie(
            10, "Seven", 127, "R",
            listOf("Morgan Freeman", "Brad Pitt"), listOf("Crime", "Drama", "Mystery"),
            "Two detectives, a rookie and a veteran, hunt a serial killer who uses the seven deadly sins as his motives."
        ),
        Movie(
            11, "Interstellar", 169, "PG-13",
            listOf("Matthew McConaughey", "Anne Hathaway"), listOf("Adventure", "Drama", "Sci-Fi"),
            "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival."
        ),
        Movie(
            12, "Parasite", 132, "R",
            listOf("Song Kang-ho", "Lee Sun-kyun"), listOf("Drama", "Thriller"),
            "Greed and class discrimination threaten the newly formed symbiotic relationship between the wealthy Park family and the destitute Kim clan."
        ),
        Movie(
            13, "The Silence of the Lambs", 118, "R",
            listOf("Jodie Foster", "Anthony Hopkins"), listOf("Crime", "Drama", "Thriller"),
            "A young F.B.I. cadet must receive the help of an incarcerated and manipulative cannibal killer to help catch another serial killer, a madman who skins his victims."
        ),
        Movie(
            14, "Spirited Away", 125, "PG",
            listOf("Rumi Hiiragi", "Miyu Irino"), listOf("Animation", "Adventure", "Family"),
            "During her family's move to the suburbs, a sullen 10-year-old girl wanders into a world ruled by gods, witches, and spirits, and where humans are changed into beasts."
        ),
        Movie(
            15, "Saving Private Ryan", 169, "R",
            listOf("Tom Hanks", "Matt Damon"), listOf("Drama", "War"),
            "Following the Normandy Landings, a group of U.S. soldiers go behind enemy lines to retrieve a paratrooper whose brothers have been killed in action."
        ),
        Movie(
            16, "The Green Mile", 189, "R",
            listOf("Tom Hanks", "Michael Clarke Duncan"), listOf("Crime", "Drama", "Fantasy"),
            "The lives of guards on Death Row are affected by one of their charges: a black man accused of child murder and rape, yet who has a mysterious gift."
        ),
        Movie(
            17, "The Lion King", 88, "G",
            listOf("Matthew Broderick", "Jeremy Irons"), listOf("Animation", "Adventure", "Drama"),
            "A Lion cub crown prince is tricked by a treacherous uncle into thinking he caused his father's death and flees into exile in despair, only to learn in adulthood his identity and his responsibilities."
        ),
        Movie(
            18, "Gladiator", 155, "R",
            listOf("Russell Crowe", "Joaquin Phoenix"), listOf("Action", "Adventure", "Drama"),
            "A former Roman General sets out to exact vengeance against the corrupt emperor who murdered his family and sent him into slavery."
        ),
        Movie(
            19, "The Departed", 151, "R",
            listOf("Leonardo DiCaprio", "Matt Damon"), listOf("Crime", "Drama", "Thriller"),
            "An undercover cop and a mole in the police attempt to identify each other while infiltrating an Irish gang in South Boston."
        ),
        Movie(
            20, "The Prestige", 130, "PG-13",
            listOf("Christian Bale", "Hugh Jackman"), listOf("Drama", "Mystery", "Sci-Fi"),
            "After a tragic accident, two stage magicians engage in a battle to create the ultimate illusion while sacrificing everything they have to outwit each other."
        )
    )

    fun getAllMovies(): List<Movie> = movies

    fun findMovieByName(name: String): Movie? {
        return movies.find { it.name.equals(name, ignoreCase = true) }
    }

    fun searchMovies(query: String): List<Movie> {
        val lowerQuery = query.lowercase()
        return movies.filter {
            it.name.lowercase().contains(lowerQuery) ||
            it.cast.any { actor -> actor.lowercase().contains(lowerQuery) } ||
            it.genres.any { genre -> genre.lowercase().contains(lowerQuery) }
        }
    }
}
