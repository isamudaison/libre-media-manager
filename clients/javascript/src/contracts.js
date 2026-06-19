/**
 * @typedef {'MOVIE'|'EPISODE'|'SERIES'|'COLLECTION'|'BOOK'|'AUDIOBOOK'|'ALBUM'|'PODCAST'|'GAME'|'OTHER'} MediaType
 */

/**
 * @typedef {'DRAFT'|'ACTIVE'|'ARCHIVED'} MediaStatus
 */

/**
 * @typedef {Object} ApiErrorResponse
 * @property {string} error
 * @property {string} message
 * @property {Record<string, string>} [fieldErrors]
 */

/**
 * @typedef {Object} MediaFileRequest
 * @property {string} location
 * @property {string | null} [mediaFileId]
 * @property {number | null} [version]
 * @property {string | null} [label]
 * @property {string | null} [mimeType]
 * @property {number | null} [sizeBytes]
 * @property {number | null} [durationSeconds]
 * @property {boolean} [primaryFile]
 */

/**
 * @typedef {Object} CreateMediaRequest
 * @property {string} title
 * @property {string | null} [parentId]
 * @property {string | null} [originalTitle]
 * @property {MediaType} mediaType
 * @property {MediaStatus | null} [status]
 * @property {string | null} [summary]
 * @property {string | null} [releaseDate]
 * @property {number | null} [runtimeMinutes]
 * @property {string | null} [language]
 * @property {MediaFileRequest[]} [mediaFiles]
 */

/**
 * @typedef {Object} UpdateMediaRequest
 * @property {string} title
 * @property {number} version
 * @property {string | null} [parentId]
 * @property {string | null} [originalTitle]
 * @property {MediaType} mediaType
 * @property {MediaStatus | null} [status]
 * @property {string | null} [summary]
 * @property {string | null} [releaseDate]
 * @property {number | null} [runtimeMinutes]
 * @property {string | null} [language]
 * @property {MediaFileRequest[]} [mediaFiles]
 */

/**
 * @typedef {Object} MediaFile
 * @property {string} mediaFileId
 * @property {string} location
 * @property {string | null} label
 * @property {string | null} mimeType
 * @property {number | null} sizeBytes
 * @property {number | null} durationSeconds
 * @property {boolean} primaryFile
 * @property {number} version
 * @property {string} createdAt
 * @property {string} updatedAt
 */

/**
 * @typedef {Object} Media
 * @property {string} mediaId
 * @property {string | null} parentId
 * @property {number} version
 * @property {string} title
 * @property {string | null} originalTitle
 * @property {MediaType} mediaType
 * @property {MediaStatus} status
 * @property {string | null} summary
 * @property {string | null} releaseDate
 * @property {number | null} runtimeMinutes
 * @property {string | null} language
 * @property {string} createdAt
 * @property {string} updatedAt
 * @property {MediaFile[]} mediaFiles
 */

/**
 * @typedef {Object} MediaPage
 * @property {Media[]} items
 * @property {number} page
 * @property {number} size
 * @property {number} totalElements
 * @property {number} totalPages
 */

/**
 * @typedef {Object} MediaListQuery
 * @property {string | null} [title]
 * @property {string | null} [parentId]
 * @property {MediaType | null} [mediaType]
 * @property {MediaStatus | null} [status]
 * @property {string | null} [language]
 * @property {string | null} [releasedBefore]
 * @property {string | null} [releasedAfter]
 * @property {number | null} [page]
 * @property {number | null} [size]
 * @property {string | null} [sort]
 * @property {string | null} [direction]
 */

export {};
