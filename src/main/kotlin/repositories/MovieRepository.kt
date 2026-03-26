package edu.teamcandy.repositories

import edu.teamcandy.models.Movie

class MovieRepository {
    private val movies = listOf(
        Movie(
            "The Shawshank Redemption", 142,
            listOf("Tim Robbins", "Morgan Freeman"), listOf("Drama"),
            "Two imprisoned men bond over a number of years, finding solace and eventual redemption through acts of common decency."
        ),
        Movie(
            "The Godfather", 175,
            listOf("Marlon Brando", "Al Pacino"), listOf("Crime", "Drama"),
            "The aging patriarch of an organized crime dynasty transfers control of his clandestine empire to his reluctant son."
        ),
        Movie(
            "The Dark Knight", 152,
            listOf("Christian Bale", "Heath Ledger"), listOf("Action", "Crime", "Drama"),
            "When the menace known as the Joker wreaks havoc and chaos on the people of Gotham, Batman must accept one of the greatest psychological and physical tests of his ability to fight injustice."
        ),
        Movie(
            "Pulp Fiction", 154,
            listOf("John Travolta", "Uma Thurman", "Samuel L. Jackson"), listOf("Crime", "Drama"),
            "The lives of two mob hitmen, a boxer, a gangster and his wife, and a pair of diner bandits intertwine in four tales of violence and redemption."
        ),
        Movie(
            "Inception", 148,
            listOf("Leonardo DiCaprio", "Joseph Gordon-Levitt"), listOf("Action", "Adventure", "Sci-Fi"),
            "A thief who steals corporate secrets through the use of dream-sharing technology is given the inverse task of planting an idea into the mind of a C.E.O."
        ),
        Movie(
            "Forrest Gump", 142,
            listOf("Tom Hanks", "Robin Wright"), listOf("Drama", "Romance"),
            "The presidencies of Kennedy and Johnson, the Vietnam War, the Watergate scandal and other historical events unfold from the perspective of an Alabama man with an IQ of 75, whose only desire is to be reunited with his childhood sweetheart."
        ),
        Movie(
            "Fight Club", 139,
            listOf("Brad Pitt", "Edward Norton"), listOf("Drama"),
            "An insomniac office worker and a devil-may-care soap maker form an underground fight club that evolves into something much, much more."
        ),
        Movie(
            "The Matrix", 136,
            listOf("Keanu Reeves", "Laurence Fishburne"), listOf("Action", "Sci-Fi"),
            "A computer hacker learns from mysterious rebels about the true nature of his reality and his role in the war against its controllers."
        ),
        Movie(
            "Goodfellas", 145,
            listOf("Robert De Niro", "Ray Liotta"), listOf("Biography", "Crime", "Drama"),
            "The story of Henry Hill and his life in the mob, covering his relationship with his wife Karen Hill and his mob partners Jimmy Conway and Tommy DeVito in the Italian-American crime syndicate."
        ),
        Movie(
            "Seven", 127,
            listOf("Morgan Freeman", "Brad Pitt"), listOf("Crime", "Drama", "Mystery"),
            "Two detectives, a rookie and a veteran, hunt a serial killer who uses the seven deadly sins as his motives."
        ),
        Movie(
            "Interstellar", 169,
            listOf("Matthew McConaughey", "Anne Hathaway"), listOf("Adventure", "Drama", "Sci-Fi"),
            "A team of explorers travel through a wormhole in space in an attempt to ensure humanity's survival."
        ),
        Movie(
            "Parasite", 132,
            listOf("Song Kang-ho", "Lee Sun-kyun"), listOf("Drama", "Thriller"),
            "Greed and class discrimination threaten the newly formed symbiotic relationship between the wealthy Park family and the destitute Kim clan."
        ),
        Movie(
            "The Silence of the Lambs", 118,
            listOf("Jodie Foster", "Anthony Hopkins"), listOf("Crime", "Drama", "Thriller"),
            "A young F.B.I. cadet must receive the help of an incarcerated and manipulative cannibal killer to help catch another serial killer, a madman who skins his victims."
        ),
        Movie(
            "Spirited Away", 125,
            listOf("Rumi Hiiragi", "Miyu Irino"), listOf("Animation", "Adventure", "Family"),
            "During her family's move to the suburbs, a sullen 10-year-old girl wanders into a world ruled by gods, witches, and spirits, and where humans are changed into beasts."
        ),
        Movie(
            "Saving Private Ryan", 169,
            listOf("Tom Hanks", "Matt Damon"), listOf("Drama", "War"),
            "Following the Normandy Landings, a group of U.S. soldiers go behind enemy lines to retrieve a paratrooper whose brothers have been killed in action."
        ),
        Movie(
            "The Green Mile", 189,
            listOf("Tom Hanks", "Michael Clarke Duncan"), listOf("Crime", "Drama", "Fantasy"),
            "The lives of guards on Death Row are affected by one of their charges: a black man accused of child murder and rape, yet who has a mysterious gift."
        ),
        Movie(
            "The Lion King", 88,
            listOf("Matthew Broderick", "Jeremy Irons"), listOf("Animation", "Adventure", "Drama"),
            "A Lion cub crown prince is tricked by a treacherous uncle into thinking he caused his father's death and flees into exile in despair, only to learn in adulthood his identity and his responsibilities."
        ),
        Movie(
            "Gladiator", 155,
            listOf("Russell Crowe", "Joaquin Phoenix"), listOf("Action", "Adventure", "Drama"),
            "A former Roman General sets out to exact vengeance against the corrupt emperor who murdered his family and sent him into slavery."
        ),
        Movie(
            "The Departed", 151,
            listOf("Leonardo DiCaprio", "Matt Damon"), listOf("Crime", "Drama", "Thriller"),
            "An undercover cop and a mole in the police attempt to identify each other while infiltrating an Irish gang in South Boston."
        ),
        Movie(
            "The Prestige", 130,
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
