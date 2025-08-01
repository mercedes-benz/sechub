import { SECHUB_CONTEXT_STORAGE_KEYS } from "../utils/sechubConstants";
import * as vscode from 'vscode';

export interface FalsePositiveCacheEntry {
    jobUUID: string;
    findingIDs: number[];
}

export class FalsePositiveCache {

    public static readonly cacheKey = SECHUB_CONTEXT_STORAGE_KEYS.falsePositiveCache;
    public static readonly cacheSizeLimit = 10;

    public static getFalsePositiveCache(context: vscode.ExtensionContext){
        const cache = context.globalState.get<FalsePositiveCacheEntry[]>(this.cacheKey);
        return cache ? cache : [];
    }

    public static addFalsePositiveEntry(context: vscode.ExtensionContext, entry: FalsePositiveCacheEntry) {
        let cache = this.getFalsePositiveCache(context);

        const existingEntry = cache.find(e => e.jobUUID === entry.jobUUID);
        if (existingEntry) {
            existingEntry.findingIDs.push(...entry.findingIDs);
            context.globalState.update(this.cacheKey, cache);
            return;
        }

        if (cache.length >= this.cacheSizeLimit) {
            cache.shift(); 
        }
        cache.push(entry);
        context.globalState.update(this.cacheKey, cache);
    }

    public static clearFalsePositiveCache(context: vscode.ExtensionContext) {
        context.globalState.update(this.cacheKey, []);
    }

    public static getEntryByJobUUID(context: vscode.ExtensionContext, jobUUID: string): FalsePositiveCacheEntry | undefined {
        const cache = this.getFalsePositiveCache(context);
        return cache.find(entry => entry.jobUUID === jobUUID);
    }

    public static removeEntryByJobUUID(context: vscode.ExtensionContext, jobUUID: string) {
        let cache = this.getFalsePositiveCache(context);
        cache = cache.filter(entry => entry.jobUUID !== jobUUID);
        context.globalState.update(this.cacheKey, cache);
    }
}


