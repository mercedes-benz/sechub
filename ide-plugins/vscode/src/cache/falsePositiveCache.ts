import { SECHUB_CONTEXT_STORAGE_KEYS } from "../utils/sechubConstants";
import * as vscode from 'vscode';

export interface FalsePositiveCacheEntry {
    jobUUID: string;
    findingIDs: number[];
}

export class FalsePositiveCache {

    public static readonly cacheKey = SECHUB_CONTEXT_STORAGE_KEYS.falsePositiveCache;
    
    /* limit the cache size as it might grow large over time and user will not need old entries after some time */
    public static readonly cacheSizeLimit = 10;

    public static getFalsePositiveCache(context: vscode.ExtensionContext){
        const cache = context.globalState.get<FalsePositiveCacheEntry[]>(this.cacheKey);
        return cache ? cache : [];
    }

    public static clearFalsePositiveCache(context: vscode.ExtensionContext) {
        context.globalState.update(this.cacheKey, []);
    }

    public static getEntryByJobUUID(context: vscode.ExtensionContext, jobUUID: string): FalsePositiveCacheEntry | undefined {
        if (!jobUUID) {
            return undefined;
        }

        const cache = this.getFalsePositiveCache(context);
        return cache.find(entry => entry.jobUUID === jobUUID);
    }

    public static removeEntryByJobUUID(context: vscode.ExtensionContext, jobUUID: string) {
        if (!jobUUID) {
            return; 
        }
        let cache = this.getFalsePositiveCache(context);
        cache = cache.filter(entry => entry.jobUUID !== jobUUID);
        context.globalState.update(this.cacheKey, cache);
    }

    public static updateCacheForEntry(context: vscode.ExtensionContext, entry: FalsePositiveCacheEntry) {
        if (!entry || !entry.jobUUID || !entry.findingIDs){
            return;
        }

        if (entry.findingIDs.length === 0) {
            // If no finding IDs, remove the entry
            this.removeEntryByJobUUID(context, entry.jobUUID);
            return;
        }

        const cache = this.getFalsePositiveCache(context);
        const existingIndex = cache.findIndex(e => e.jobUUID === entry.jobUUID);
        if (existingIndex !== -1) {
            cache[existingIndex] = entry;
        } else {
            cache.push(entry);
        }
        context.globalState.update(this.cacheKey, cache);
    }

}


